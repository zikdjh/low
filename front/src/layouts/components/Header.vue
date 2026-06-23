<template>
  <header ref="headerRef" class="layout-header">
    <!-- 左侧：折叠按钮 + 工作区选择器 -->
    <div class="header-left">
      <t-button
        variant="text"
        size="large"
        @click="toggleSidebar"
        class="sidebar-toggle"
      >
        <MenuIcon />
      </t-button>

      <!-- 工作区/应用选择器 -->
      <div ref="workspaceTriggerRef" class="workspace-switcher" @click="showWorkspaceMenu = !showWorkspaceMenu">
        <div class="workspace-icon">
          <LayoutIcon size="18" />
        </div>
        <div class="workspace-info">
          <span class="workspace-name">我的应用</span>
          <span class="workspace-badge">PRO</span>
        </div>
        <ChevronDownIcon class="workspace-arrow" :class="{ rotated: showWorkspaceMenu }" />
      </div>

      <!-- 工作区下拉 -->
      <div ref="workspaceDropdownRef" v-show="showWorkspaceMenu" class="workspace-dropdown" @click.stop>
        <div class="ws-dropdown-header">切换工作区</div>
        <div class="ws-dropdown-list">
          <div
            v-for="ws in workspaces"
            :key="ws.id"
            class="ws-dropdown-item"
            :class="{ active: ws.active }"
            @click="switchWorkspace(ws)"
          >
            <div class="ws-item-icon" :style="{ background: ws.color }">
              <component :is="ws.icon" size="14" />
            </div>
            <div class="ws-item-info">
              <span class="ws-item-name">{{ ws.name }}</span>
              <span class="ws-item-desc">{{ ws.desc }}</span>
            </div>
            <CheckIcon v-if="ws.active" size="16" class="ws-check" />
          </div>
        </div>
        <div class="ws-dropdown-footer">
          <t-button variant="text" size="small" block @click="showWorkspaceMenu = false">
            <template #icon><AddIcon /></template>
            新建工作区
          </t-button>
        </div>
      </div>
    </div>

    <!-- 中间：全局搜索 / 命令面板 -->
    <div class="header-center">
      <div class="search-box" @click="openCommandPalette">
        <SearchIcon class="search-icon" />
        <span class="search-placeholder">搜索页面、实体、组件、命令...</span>
        <kbd class="search-shortcut">Ctrl+K</kbd>
      </div>
    </div>

    <!-- 右侧：操作按钮 + 通知 + 主题 + 用户 -->
    <div class="header-right">
      <!-- 在线协作状态 -->
      <div class="collab-indicator" v-if="onlineCount > 0">
        <div class="collab-dot"></div>
        <span class="collab-count">{{ onlineCount }}</span>
      </div>

      <!-- 预览 -->
      <t-tooltip content="预览应用">
        <t-button variant="text" class="header-action-btn" @click="handlePreview">
          <BrowseIcon size="18" />
          <span class="btn-label">预览</span>
        </t-button>
      </t-tooltip>

      <!-- 发布 -->
      <t-tooltip content="发布到生产环境">
        <t-button variant="text" class="header-action-btn header-action-publish" @click="handlePublish">
          <RocketIcon size="18" />
          <span class="btn-label">发布</span>
        </t-button>
      </t-tooltip>

      <div class="header-divider"></div>

      <!-- 管理后台入口（仅管理员可见） -->
      <t-tooltip v-if="userStore.isAdmin" content="管理后台">
        <t-button variant="text" class="header-action-btn header-action-admin" @click="goToAdmin">
          <SecuredIcon size="18" />
          <span class="btn-label">管理后台</span>
        </t-button>
      </t-tooltip>

      <!-- 主题切换 -->
      <t-tooltip :content="isDark ? '切换亮色模式' : '切换暗色模式'">
        <t-button variant="text" class="icon-btn" @click="toggleTheme">
          <component :is="isDark ? SunnyIcon : MoonIcon" />
        </t-button>
      </t-tooltip>

      <!-- 通知 -->
      <t-badge :count="notifStore.unreadCount" :offset="[-2, 4]" :dot="false">
        <t-button variant="text" class="icon-btn" @click="showNotifications = true">
          <NotificationIcon />
        </t-button>
      </t-badge>

      <!-- 设置 -->
      <t-tooltip content="系统设置">
        <t-button variant="text" class="icon-btn" @click="goToSettings">
          <SettingIcon />
        </t-button>
      </t-tooltip>

      <!-- 用户 -->
      <div ref="userTriggerRef" class="user-dropdown">
        <t-button variant="text" class="user-btn" @click="toggleUserMenu">
          <div class="user-info">
            <t-avatar size="32px" class="user-avatar">
              <template #icon><UserIcon /></template>
            </t-avatar>
            <span class="user-name">{{ userName }}</span>
            <ChevronDownIcon class="user-arrow" :class="{ rotated: showUserMenu }" />
          </div>
        </t-button>
        <div ref="userDropdownRef" v-show="showUserMenu" class="dropdown-menu" @click.stop>
          <div class="dropdown-header">
            <t-avatar size="40px">
              <template #icon><UserIcon /></template>
            </t-avatar>
            <div>
              <div class="dropdown-user-name">{{ userName }}</div>
              <div class="dropdown-user-role">管理员</div>
            </div>
          </div>
          <div class="dropdown-divider"></div>
          <div
            v-for="item in userMenuItems"
            :key="item.value"
            class="dropdown-item"
            @click="handleUserMenuClick(item.value)"
          >
            <component :is="item.icon" size="16" />
            <span>{{ item.label }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 命令面板弹窗 ========== -->
    <teleport to="body">
      <div v-if="showCommandPalette" class="cmd-overlay" @click="showCommandPalette = false">
        <div class="cmd-palette" @click.stop>
          <div class="cmd-search-bar">
            <SearchIcon size="18" class="cmd-search-icon" />
            <input
              ref="cmdInput"
              v-model="cmdQuery"
              type="text"
              class="cmd-input"
              placeholder="输入关键词搜索..."
              @keydown.esc="showCommandPalette = false"
              @keydown.enter="executeCmdAction"
              @keydown.up.prevent="navigateCmd(-1)"
              @keydown.down.prevent="navigateCmd(1)"
            />
            <kbd class="cmd-key">ESC</kbd>
          </div>
          <div class="cmd-groups" v-if="filteredCmdItems.length > 0">
            <div
              v-for="group in groupedCmdItems"
              :key="group.label"
              class="cmd-group"
            >
              <div class="cmd-group-label">{{ group.label }}</div>
              <div
                v-for="(item, idx) in group.items"
                :key="item.id"
                class="cmd-item"
                :class="{ active: cmdActiveIndex === getGlobalCmdIndex(group, idx) }"
                @click="runCmdAction(item)"
                @mouseenter="cmdActiveIndex = getGlobalCmdIndex(group, idx)"
              >
                <div class="cmd-item-icon">
                  <component :is="item.icon" size="16" />
                </div>
                <div class="cmd-item-content">
                  <span class="cmd-item-label">{{ item.label }}</span>
                  <span class="cmd-item-desc">{{ item.description }}</span>
                </div>
                <span class="cmd-item-shortcut" v-if="item.shortcut">{{ item.shortcut }}</span>
              </div>
            </div>
          </div>
          <div v-else class="cmd-empty">
            <SearchIcon size="32" class="cmd-empty-icon" />
            <p>未找到匹配结果</p>
          </div>
        </div>
      </div>
    </teleport>

  </header>

  <!-- ========== 通知面板（attach="body" 确保不被 Header 的 backdrop-filter 层叠上下文裁剪） ========== -->
  <t-drawer
    v-model:visible="showNotifications"
    title="通知中心"
    :size="420"
    placement="right"
    attach="body"
  >
      <div class="notification-panel">
        <div class="notification-header">
          <span>全部通知</span>
          <div class="notification-actions">
            <t-button variant="text" size="small" @click="notifStore.markAllAsRead()">全部已读</t-button>
            <t-button variant="text" size="small" @click="goToNotifications">查看全部 →</t-button>
          </div>
        </div>
        <div class="notification-list">
          <div
            v-for="item in notifStore.notifications.slice(0, 10)"
            :key="item.id"
            class="notification-item"
            :class="{ unread: !item.read }"
            @click="notifStore.markAsRead(item.id)"
          >
            <div class="notification-icon" :style="{ background: getNotifBg(item.type) }">
              <component :is="getNotifIcon(item.type)" />
            </div>
            <div class="notification-content">
              <p class="notification-title">{{ item.title }}</p>
              <p class="notification-desc">{{ item.description }}</p>
              <span class="notification-time">{{ item.time }}</span>
            </div>
            <div v-if="!item.read" class="unread-dot"></div>
          </div>
        </div>
      </div>
    </t-drawer>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  MenuIcon, SettingIcon, SearchIcon, AddIcon,
  MoonIcon, UserIcon, ChevronDownIcon, CheckIcon,
  CheckCircleIcon, TipsIcon, ErrorCircleFilledIcon,
  InfoCircleFilledIcon, NotificationIcon, SunnyIcon,
  UserCircleIcon, LogoutIcon, HelpCircleIcon,
  LayoutIcon, AppIcon, DataBaseIcon,
  BrowseIcon, RocketIcon, EditIcon, HomeIcon, SecuredIcon
} from 'tdesign-icons-vue-next';
import { useSettingStore, useUserStore, useNotificationStore } from '../../store';
import { smoothThemeTransition } from '../../utils/theme';

const router = useRouter();
const route = useRoute();
void route; // used in template
const settingStore = useSettingStore();
const userStore = useUserStore();
const notifStore = useNotificationStore();

const showUserMenu = ref(false);
const showNotifications = ref(false);
const showWorkspaceMenu = ref(false);
const showCommandPalette = ref(false);
const cmdQuery = ref('');
const cmdActiveIndex = ref(0);
const cmdInput = ref<HTMLInputElement | null>(null);
const onlineCount = ref(1); // 模拟在线协作人数

const userName = computed(() => userStore.displayName);
const isDark = computed(() => settingStore.displayMode === 'dark');

// 工作区模拟数据
const workspaces = ref([
  { id: 1, name: '我的应用', desc: '主工作区', icon: AppIcon, color: 'linear-gradient(135deg, #f5a623, #e8a317)', active: true },
  { id: 2, name: '后台管理', desc: '内部管理系统', icon: LayoutIcon, color: 'linear-gradient(135deg, #1677ff, #69b1ff)', active: false },
  { id: 3, name: '数据中台', desc: '数据分析平台', icon: DataBaseIcon, color: 'linear-gradient(135deg, #52c41a, #73d13d)', active: false },
]);

function switchWorkspace(ws: any) {
  workspaces.value.forEach(w => (w.active = w.id === ws.id));
  showWorkspaceMenu.value = false;
}

// 命令面板数据
interface CmdItem {
  id: string;
  label: string;
  description: string;
  icon: any;
  action: () => void;
  shortcut?: string;
  group: string;
}

const cmdItems: CmdItem[] = [
  // 页面
  { id: 'new-page', label: '新建页面', description: '创建新的低代码页面', icon: AddIcon, action: () => router.push('/lowcode/page/list'), shortcut: 'Ctrl+N', group: '页面' },
  { id: 'list-pages', label: '页面列表', description: '查看所有页面', icon: LayoutIcon, action: () => router.push('/lowcode/page/list'), group: '页面' },
  { id: 'design-page', label: '页面设计器', description: '进入可视化设计器', icon: EditIcon, action: () => router.push('/lowcode/page/design'), group: '页面' },
  // 数据模型
  { id: 'new-entity', label: '新建实体', description: '创建数据实体模型', icon: AddIcon, action: () => router.push('/lowcode/entity'), group: '数据模型' },
  { id: 'list-entities', label: '实体管理', description: '管理所有数据实体', icon: DataBaseIcon, action: () => router.push('/lowcode/entity'), group: '数据模型' },
  // 导航
  { id: 'go-home', label: '返回首页', description: '前往仪表盘', icon: HomeIcon, action: () => router.push('/home'), group: '导航' },
  { id: 'go-notifications', label: '通知中心', description: '查看系统通知', icon: NotificationIcon, action: () => router.push('/lowcode/notification'), group: '导航' },
  { id: 'go-settings', label: '系统设置', description: '平台配置', icon: SettingIcon, action: () => router.push('/lowcode/settings'), shortcut: 'Ctrl+,', group: '导航' },
  // 主题
  { id: 'toggle-theme', label: '切换主题', description: '切换暗色/亮色模式', icon: MoonIcon, action: () => toggleTheme(), shortcut: 'Ctrl+T', group: '系统' },
];

const filteredCmdItems = computed(() => {
  const q = cmdQuery.value.toLowerCase().trim();
  if (!q) return cmdItems;
  return cmdItems.filter(item =>
    item.label.toLowerCase().includes(q) ||
    item.description.toLowerCase().includes(q) ||
    item.group.toLowerCase().includes(q)
  );
});

const groupedCmdItems = computed(() => {
  const groups: Record<string, CmdItem[]> = {};
  filteredCmdItems.value.forEach(item => {
    if (!groups[item.group]) groups[item.group] = [];
    groups[item.group].push(item);
  });
  return Object.entries(groups).map(([label, items]) => ({ label, items }));
});

function getGlobalCmdIndex(group: { label: string; items: CmdItem[] }, localIdx: number): number {
  let offset = 0;
  for (const g of groupedCmdItems.value) {
    if (g.label === group.label) return offset + localIdx;
    offset += g.items.length;
  }
  return 0;
}

function getTotalCmdCount(): number {
  return filteredCmdItems.value.length;
}

function navigateCmd(dir: number) {
  const total = getTotalCmdCount();
  if (total === 0) return;
  cmdActiveIndex.value = (cmdActiveIndex.value + dir + total) % total;
}

function executeCmdAction() {
  const total = getTotalCmdCount();
  if (total === 0) return;
  let idx = 0;
  for (const group of groupedCmdItems.value) {
    for (const item of group.items) {
      if (idx === cmdActiveIndex.value) {
        runCmdAction(item);
        return;
      }
      idx++;
    }
  }
  // fallback
  runCmdAction(filteredCmdItems.value[cmdActiveIndex.value] || filteredCmdItems.value[0]);
}

function runCmdAction(item: CmdItem) {
  showCommandPalette.value = false;
  cmdQuery.value = '';
  item.action();
}

function openCommandPalette() {
  showCommandPalette.value = true;
  cmdQuery.value = '';
  cmdActiveIndex.value = 0;
  nextTick(() => cmdInput.value?.focus());
}

watch(showCommandPalette, (v) => {
  if (v) {
    cmdQuery.value = '';
    cmdActiveIndex.value = 0;
  }
});
watch(cmdQuery, () => { cmdActiveIndex.value = 0; });

// 键盘快捷键
function onKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
    e.preventDefault();
    openCommandPalette();
  }
}

// 点击其他区域关闭下拉菜单
const headerRef = ref<HTMLElement | null>(null);
const workspaceTriggerRef = ref<HTMLElement | null>(null);
const workspaceDropdownRef = ref<HTMLElement | null>(null);
const userTriggerRef = ref<HTMLElement | null>(null);
const userDropdownRef = ref<HTMLElement | null>(null);

function onDocumentClick(e: MouseEvent) {
  const target = e.target as HTMLElement;
  // 点击不在工作区触发器 & 不在工作区下拉内 → 关闭工作区菜单
  if (
    !workspaceTriggerRef.value?.contains(target) &&
    !workspaceDropdownRef.value?.contains(target)
  ) {
    showWorkspaceMenu.value = false;
  }
  // 点击不在用户触发器 & 不在用户下拉内 → 关闭用户菜单
  if (
    !userTriggerRef.value?.contains(target) &&
    !userDropdownRef.value?.contains(target)
  ) {
    showUserMenu.value = false;
  }
}

onMounted(() => {
  userStore.restoreSession();
  notifStore.loadNotifications();
  document.addEventListener('keydown', onKeydown);
  document.addEventListener('click', onDocumentClick);
});
onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown);
  document.removeEventListener('click', onDocumentClick);
});

const userMenuItems = [
  { label: '个人设置', value: 'profile', icon: UserCircleIcon },
  { label: '帮助中心', value: 'help', icon: HelpCircleIcon },
  { label: '退出登录', value: 'logout', icon: LogoutIcon },
];

function toggleSidebar() { settingStore.toggleSidebar(); }

function toggleTheme() {
  smoothThemeTransition(() => {
    const newMode = isDark.value ? 'light' : 'dark';
    settingStore.updateConfig({ mode: newMode });
  });
}

function handlePreview() {
  router.push('/lowcode/page/view');
}
function handlePublish() {
  // 简单模拟发布
  const msg = '应用发布成功！生产环境已更新。';
  notifStore.addNotification({
    title: '发布成功',
    description: msg,
    type: 'success',
    category: 'system',
  });
}
function goToSettings() { router.push('/lowcode/settings'); }
function goToAdmin() { router.push('/admin/users'); }
function goToNotifications() {
  showNotifications.value = false;
  router.push('/lowcode/notification');
}
function toggleUserMenu() { showUserMenu.value = !showUserMenu.value; }
function handleUserMenuClick(value: string) {
  if (value === 'logout') {
    void userStore.logout();
  } else if (value === 'profile') {
    router.push('/lowcode/settings');
  } else if (value === 'help') {
    router.push('/lowcode/help');
  }
  showUserMenu.value = false;
}

// 通知图标映射
function getNotifIcon(type: string) {
  switch (type) {
    case 'success': return CheckCircleIcon;
    case 'warning': return ErrorCircleFilledIcon;
    case 'error': return ErrorCircleFilledIcon;
    case 'system': return TipsIcon;
    default: return InfoCircleFilledIcon;
  }
}
function getNotifBg(type: string) {
  switch (type) {
    case 'success': return 'linear-gradient(135deg, #52c41a, #73d13d)';
    case 'warning': return 'linear-gradient(135deg, #fa8c16, #ffa940)';
    case 'error': return 'linear-gradient(135deg, #ff4d4f, #ff7875)';
    case 'system': return 'linear-gradient(135deg, #f5a623, #e8a317)';
    default: return 'linear-gradient(135deg, #1677ff, #69b1ff)';
  }
}
</script>

<style scoped lang="less">
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(8px);
  gap: 12px;
}

/* ===== 左侧 ===== */
.header-left {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.sidebar-toggle {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  color: #666;
  flex-shrink: 0;
  &:hover { background: #f3f4f6; color: #333; }
}

/* 工作区选择器 */
.workspace-switcher {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
  user-select: none;

  &:hover {
    background: #f3f4f6;
    border-color: #e5e7eb;
  }
}
.workspace-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--td-brand-color, #E8A317);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.workspace-info {
  display: flex;
  flex-direction: column;
  gap: 0;
  min-width: 0;
}
.workspace-name {
  font-size: 13px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.3;
}
.workspace-badge {
  font-size: 9px;
  font-weight: 700;
  color: var(--td-brand-color, #E8A317);
  background: rgba(232, 163, 23, 0.1);
  padding: 0 5px;
  border-radius: 3px;
  line-height: 1.4;
  width: fit-content;
}
.workspace-arrow {
  font-size: 14px;
  color: #999;
  transition: transform 0.3s;
  &.rotated { transform: rotate(180deg); }
}

/* 工作区下拉 */
.workspace-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 48px;
  min-width: 260px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.12), 0 2px 8px rgba(0,0,0,0.06);
  padding: 6px;
  z-index: 1000;
  animation: menuSlide 0.15s ease-out;
}
.ws-dropdown-header {
  padding: 10px 12px 6px;
  font-size: 11px;
  font-weight: 600;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.ws-dropdown-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.ws-dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
  &:hover { background: #f9fafb; }
  &.active { background: rgba(232, 163, 23, 0.06); }
}
.ws-item-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.ws-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.ws-item-name { font-size: 13px; font-weight: 600; color: #1a1a1a; }
.ws-item-desc { font-size: 11px; color: #999; }
.ws-check { color: var(--td-brand-color, #E8A317); }
.ws-dropdown-footer {
  padding: 6px 4px 4px;
  border-top: 1px solid #f3f4f6;
  margin-top: 6px;
}

@keyframes menuSlide {
  from { opacity: 0; transform: translateY(-6px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== 中间搜索 ===== */
.header-center {
  flex: 1;
  display: flex;
  align-items: center;
  max-width: 420px;
  justify-content: center;
}
.search-box {
  display: flex;
  align-items: center;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 7px 12px;
  transition: all 0.2s;
  width: 100%;
  border: 1.5px solid transparent;
  cursor: pointer;
  user-select: none;

  &:hover {
    background: #eef0f2;
    border-color: #e0e0e0;
  }
  &:focus-within {
    background: #fff;
    border-color: var(--td-brand-color, #E8A317);
    box-shadow: 0 0 0 3px rgba(232, 163, 23, 0.06);
  }
}
.search-icon { color: #999; margin-right: 8px; font-size: 16px; flex-shrink: 0; }
.search-placeholder { color: #aaa; font-size: 13px; flex: 1; }
.search-shortcut {
  margin-left: 6px;
  padding: 2px 7px;
  font-size: 11px;
  color: #999;
  background: #e8e8e8;
  border-radius: 4px;
  font-family: inherit;
  flex-shrink: 0;
  line-height: 1.5;
}

/* ===== 右侧 ===== */
.header-right {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
}

.collab-indicator {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  background: #f0fdf4;
  border-radius: 10px;
  margin-right: 4px;
}
.collab-dot {
  width: 7px; height: 7px;
  background: #52c41a;
  border-radius: 50%;
  animation: pulse-dot 1.5s ease infinite;
}
@keyframes pulse-dot {
  0%, 100% { box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.4); }
  50% { box-shadow: 0 0 0 4px rgba(82, 196, 26, 0); }
}
.collab-count { font-size: 12px; font-weight: 600; color: #52c41a; }

.header-action-btn {
  height: 32px;
  padding: 0 12px;
  border-radius: 6px;
  color: #555;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 5px;
  .btn-label { font-size: 12px; font-weight: 500; }
  &:hover { background: #f3f4f6; color: #333; }
}
.header-action-publish {
  color: #fff;
  background: var(--td-brand-color, #E8A317);
  border-radius: 6px;
  margin-left: 2px;
  &:hover {
    background: var(--td-brand-color-7, #d4920a) !important;
    color: #fff !important;
  }
}
.header-action-admin {
  color: #1677ff;
  border-radius: 6px;
  &:hover { background: #e6f4ff !important; color: #1677ff !important; }
}

.header-divider {
  width: 1px;
  height: 24px;
  background: #e5e7eb;
  margin: 0 6px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  &:hover { background: #f3f4f6; color: var(--td-brand-color, #E8A317); }
  :deep(.t-icon) { font-size: 20px; }
}

/* ===== 用户下拉 ===== */
.user-dropdown { position: relative; }
.user-btn { padding: 0; }
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  &:hover { background: #f3f4f6; }
}
.user-avatar {
  background: linear-gradient(135deg, #f5a623, #e8a317) !important;
  :deep(.t-icon) { font-size: 16px; }
}
.user-name {
  font-size: 13px;
  font-weight: 500;
  color: #333;
}
.user-arrow {
  font-size: 14px;
  color: #999;
  transition: transform 0.3s;
  &.rotated { transform: rotate(180deg); }
}
.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 200px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  padding: 6px;
  z-index: 1000;
  animation: menuSlide 0.15s ease-out;
}
.dropdown-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 10px 12px;
}
.dropdown-user-name { font-size: 14px; font-weight: 600; color: #1a1a1a; }
.dropdown-user-role { font-size: 12px; color: #999; margin-top: 2px; }
.dropdown-divider { height: 1px; background: #f0f0f0; margin: 4px 0; }
.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.15s;
  &:hover { background: #f9fafb; color: var(--td-brand-color, #E8A317); }
}

/* ===== 命令面板 ===== */
.cmd-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(2px);
  z-index: 9999;
  display: flex;
  justify-content: center;
  padding-top: 15vh;
  animation: fadeIn 0.15s ease;
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
.cmd-palette {
  width: 560px;
  max-width: 90vw;
  max-height: 460px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideUp 0.2s ease;
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(12px) scale(0.97); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.cmd-search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
}
.cmd-search-icon { color: #999; flex-shrink: 0; }
.cmd-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  color: #1a1a1a;
  background: transparent;
  &::placeholder { color: #bbb; }
}
.cmd-key {
  padding: 3px 8px;
  font-size: 11px;
  color: #999;
  background: #f3f4f6;
  border-radius: 4px;
  font-family: inherit;
}

.cmd-groups {
  overflow-y: auto;
  padding: 6px;
  flex: 1;
  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: #d0d5dd; border-radius: 4px; }
}
.cmd-group { margin-bottom: 4px; }
.cmd-group-label {
  padding: 6px 12px 4px;
  font-size: 11px;
  font-weight: 600;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.cmd-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.12s;
  &.active,
  &:hover { background: #f5f5f5; }
}
.cmd-item-icon {
  width: 32px; height: 32px;
  border-radius: 8px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  flex-shrink: 0;
}
.cmd-item.active .cmd-item-icon,
.cmd-item:hover .cmd-item-icon {
  background: rgba(232, 163, 23, 0.1);
  color: var(--td-brand-color, #E8A317);
}
.cmd-item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.cmd-item-label { font-size: 13px; font-weight: 500; color: #1a1a1a; }
.cmd-item-desc { font-size: 11px; color: #999; }
.cmd-item-shortcut {
  font-size: 11px;
  color: #bbb;
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 4px;
}
.cmd-empty {
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #ccc;
  .cmd-empty-icon { opacity: 0.5; }
  p { font-size: 13px; color: #999; margin: 0; }
}

/* ===== 通知面板 ===== */
.notification-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 0 16px;
  font-weight: 600;
  font-size: 15px;
  color: #1a1a1a;
}
.notification-actions { display: flex; gap: 4px; }
.notification-list { flex: 1; overflow-y: auto; }
.notification-item {
  display: flex;
  gap: 12px;
  padding: 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
  &:hover { background: #fafafa; }
  &.unread { background: #fffdf5; }
}
.notification-icon {
  width: 40px; height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  :deep(.t-icon) { color: #fff; font-size: 18px; }
}
.notification-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}
.notification-title { font-size: 14px; font-weight: 600; color: #1a1a1a; margin: 0; }
.notification-desc { font-size: 13px; color: #777; margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.notification-time { font-size: 12px; color: #bbb; }
.unread-dot {
  position: absolute;
  top: 18px; right: 14px;
  width: 8px; height: 8px;
  background: var(--td-brand-color, #E8A317);
  border-radius: 50%;
}
</style>
