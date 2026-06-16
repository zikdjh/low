<template>
  <header class="layout-header">
    <div class="header-left">
      <t-button
        variant="text"
        size="large"
        @click="toggleSidebar"
        class="sidebar-toggle"
      >
        <MenuIcon />
      </t-button>
      <div class="header-brand">
        <svg viewBox="0 0 32 32" fill="none" class="brand-logo">
          <rect width="32" height="32" rx="8" fill="url(#hgrad)"/>
          <path d="M10 11.5l6-3 6 3-6 3-6-3z" fill="#fff" opacity="0.9"/>
          <path d="M10 16l6 3 6-3M10 20.5l6 3 6-3" stroke="#fff" stroke-width="1.8" stroke-linecap="round" opacity="0.8"/>
          <defs>
            <linearGradient id="hgrad" x1="0" y1="0" x2="32" y2="32">
              <stop offset="0%" stop-color="#F5A623"/>
              <stop offset="100%" stop-color="#E8A317"/>
            </linearGradient>
          </defs>
        </svg>
        <span class="brand-text">LowCode</span>
      </div>
    </div>

    <div class="header-center">
      <div class="search-box">
        <SearchIcon class="search-icon" />
        <input
          type="text"
          class="search-input"
          placeholder="搜索页面、组件、实体..."
          v-model="searchText"
          @keyup.enter="handleSearch"
        />
        <t-button
          v-if="searchText"
          variant="text"
          size="small"
          class="clear-btn"
          @click="searchText = ''"
        >
          <CloseIcon size="14" />
        </t-button>
        <kbd class="search-shortcut">⌘K</kbd>
      </div>
    </div>

    <div class="header-right">
      <!-- 主题切换 -->
      <t-tooltip :content="isDarkTheme ? '切换亮色模式' : '切换暗色模式'">
        <t-button variant="text" class="icon-btn" @click="toggleTheme">
          <component :is="isDarkTheme ? SunnyIcon : MoonIcon" />
        </t-button>
      </t-tooltip>

      <!-- 通知 -->
      <t-badge :count="unreadCount" :offset="[-2, 4]">
        <t-button variant="text" class="icon-btn" @click="showNotifications = true">
          <NotificationIcon />
        </t-button>
      </t-badge>

      <!-- 设置 -->
      <t-tooltip content="设置">
        <t-button variant="text" class="icon-btn" @click="goToSettings">
          <SettingIcon />
        </t-button>
      </t-tooltip>

      <!-- 用户 -->
      <div class="user-dropdown">
        <t-button variant="text" class="user-btn" @click="toggleUserMenu">
          <div class="user-info">
            <t-avatar size="32px" class="user-avatar">
              <template #icon><UserIcon /></template>
            </t-avatar>
            <span class="user-name">{{ userName }}</span>
            <ChevronDownIcon class="user-arrow" :class="{ rotated: showUserMenu }" />
          </div>
        </t-button>
        <div v-show="showUserMenu" class="dropdown-menu" @click.stop>
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

    <!-- 通知面板 -->
    <t-drawer
      v-model="showNotifications"
      title="通知中心"
      :width="420"
      placement="right"
    >
      <div class="notification-panel">
        <div class="notification-header">
          <span>全部通知</span>
          <t-button variant="text" size="small" @click="markAllRead">全部已读</t-button>
        </div>
        <div class="notification-list">
          <div
            v-for="(item, index) in notifications"
            :key="index"
            class="notification-item"
            :class="{ unread: !item.read }"
            @click="markAsRead(index)"
          >
            <div class="notification-icon" :style="{ background: item.bgColor }">
              <component :is="item.icon" />
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
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  MenuIcon, SettingIcon, SearchIcon, CloseIcon,
  MoonIcon, UserIcon, ChevronDownIcon,
  CheckCircleIcon, TipsIcon, FileIcon,
  NotificationIcon, SunnyIcon, UserCircleIcon, LogoutIcon
} from 'tdesign-icons-vue-next';
import { useSettingStore, useUserStore } from '../../store';

const router = useRouter();
const route = useRoute();
const settingStore = useSettingStore();
const userStore = useUserStore();

const searchText = ref('');
const isDarkTheme = ref(false);
const showUserMenu = ref(false);
const showNotifications = ref(false);
const userName = computed(() => userStore.displayName);
const unreadCount = ref(3);

onMounted(() => {
  userStore.restoreSession();
});

const notifications = ref([
  {
    title: '系统更新',
    description: '低代码平台已更新至 v2.0.0，全新黄色主题上线',
    time: '5分钟前',
    read: false,
    icon: TipsIcon,
    bgColor: 'linear-gradient(135deg, #f5a623, #e8a317)'
  },
  {
    title: '任务完成',
    description: '您创建的页面"首页"已成功发布',
    time: '15分钟前',
    read: false,
    icon: CheckCircleIcon,
    bgColor: 'linear-gradient(135deg, #52c41a, #73d13d)'
  },
  {
    title: '新消息',
    description: '团队成员邀请您协作编辑页面',
    time: '1小时前',
    read: true,
    icon: FileIcon,
    bgColor: 'linear-gradient(135deg, #1677ff, #69b1ff)'
  }
]);

const userMenuItems = [
  { label: '个人信息', value: 'profile', icon: UserCircleIcon },
  { label: '退出登录', value: 'logout', icon: LogoutIcon }
];

function toggleSidebar() { settingStore.toggleSidebar(); }
function toggleTheme() {
  isDarkTheme.value = !isDarkTheme.value;
  document.body.classList.toggle('dark-theme', isDarkTheme.value);
}
function handleSearch() {
  const keyword = searchText.value.trim().toLowerCase();
  if (!keyword) return;
  // 智能搜索：根据关键词跳转到对应模块
  if (keyword.includes('实体') || keyword.includes('entity') || keyword.includes('数据模型')) {
    router.push('/lowcode/entity');
  } else if (keyword.includes('页面') || keyword.includes('设计') || keyword.includes('page')) {
    router.push('/lowcode/page/list');
  } else if (keyword.includes('数据') || keyword.includes('data')) {
    router.push('/lowcode/entity');
  } else {
    router.push('/lowcode/page/list');
  }
  searchText.value = '';
}
function goToSettings() { router.push('/lowcode/entity'); }
function toggleUserMenu() { showUserMenu.value = !showUserMenu.value; }
function handleUserMenuClick(value: string) {
  if (value === 'logout') {
    userStore.logout();
  }
  showUserMenu.value = false;
}
function markAllRead() {
  notifications.value.forEach(n => n.read = true);
  unreadCount.value = 0;
}
function markAsRead(index: number) {
  if (!notifications.value[index].read) {
    notifications.value[index].read = true;
    unreadCount.value--;
  }
}
</script>

<style scoped lang="less">
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(8px);
}

/* ===== 左侧 ===== */
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 260px;
}
.header-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}
.brand-logo { width: 32px; height: 32px; flex-shrink: 0; }
.brand-text {
  font-size: 17px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: -0.3px;
}
.sidebar-toggle {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  color: #666;
  &:hover { background: #f5f5f5; color: #333; }
}

/* ===== 中间搜索 ===== */
.header-center {
  flex: 1;
  display: flex;
  align-items: center;
  max-width: 480px;
  margin: 0 24px;
}
.search-box {
  position: relative;
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 10px;
  padding: 8px 14px;
  transition: all 0.25s;
  width: 100%;
  border: 1.5px solid transparent;

  &:focus-within {
    background: #fff;
    border-color: var(--td-brand-color, #E8A317);
    box-shadow: 0 0 0 3px rgba(232, 163, 23, 0.08);
  }
}
.search-icon { color: #999; margin-right: 8px; font-size: 16px; flex-shrink: 0; }
.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 14px;
  color: #333;
  &::placeholder { color: #bbb; }
}
.clear-btn { padding: 2px; color: #bbb; flex-shrink: 0; }
.search-shortcut {
  margin-left: 8px;
  padding: 2px 7px;
  font-size: 11px;
  color: #999;
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 5px;
  font-family: inherit;
  flex-shrink: 0;
}

/* ===== 右侧 ===== */
.header-right {
  display: flex;
  align-items: center;
  gap: 4px;
}
.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  &:hover { background: #f5f5f5; color: var(--td-brand-color, #E8A317); }
  :deep(.t-icon) { font-size: 20px; }
}

/* ===== 用户下拉 ===== */
.user-dropdown { position: relative; }
.user-btn { padding: 0; }
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
  &:hover { background: #f5f5f5; }
}
.user-avatar {
  background: linear-gradient(135deg, #f5a623, #e8a317) !important;
  :deep(.t-icon) { font-size: 16px; }
}
.user-name {
  font-size: 14px;
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
@keyframes menuSlide {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
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
  font-size: 14px;
  color: #555;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.15s;
  &:hover { background: #f9fafb; color: var(--td-brand-color, #E8A317); }
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
  width: 40px;
  height: 40px;
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
  top: 18px;
  right: 14px;
  width: 8px;
  height: 8px;
  background: var(--td-brand-color, #E8A317);
  border-radius: 50%;
}
</style>
