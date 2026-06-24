<template>
  <div class="app-runtime">
    <!-- ===== 左侧导航栏 ===== -->
    <aside class="ar-sidebar">
      <!-- 应用头部 -->
      <div class="ar-sidebar-brand">
        <div class="ar-brand-icon" :style="{ background: app?.color || 'linear-gradient(135deg, #667eea, #764ba2)' }">
          <AppIcon size="22" />
        </div>
        <div class="ar-brand-info">
          <h3>{{ app?.name || '业务应用' }}</h3>
        </div>
      </div>

      <!-- 导航菜单（多级树） -->
      <nav class="ar-nav">
        <div class="ar-nav-label">导航菜单</div>

        <!-- 一级节点 -->
        <template v-for="root in menuTree" :key="`r-${root.id || root.pageCode || root.name}`">
          <!-- 分组节点（带 children）：可折叠 -->
          <template v-if="root.children && root.children.length > 0">
            <div
              class="ar-nav-group-header"
              :class="{ collapsed: collapsedGroups[String(root.id)] }"
              @click="toggleGroup(root)"
            >
              <ChevronRightIcon
                size="14"
                class="ar-nav-arrow"
                :class="{ expanded: !collapsedGroups[String(root.id)] }"
              />
              <FolderIcon size="16" class="ar-nav-icon" />
              <span class="ar-nav-text">{{ root.name }}</span>
            </div>
            <div v-show="!collapsedGroups[String(root.id)]" class="ar-nav-children">
              <div
                v-for="leaf in root.children"
                :key="`l-${leaf.id || leaf.pageCode}`"
                class="ar-nav-item ar-nav-item-sub"
                :class="{ active: isLeafActive(leaf) }"
                @click="switchPage(leaf)"
              >
                <div class="ar-nav-dot" :class="{ on: isLeafActive(leaf) }"></div>
                <FileIcon size="14" class="ar-nav-icon" />
                <span class="ar-nav-text">{{ leaf.name }}</span>
              </div>
            </div>
          </template>

          <!-- 叶子根节点：直接渲染 -->
          <div
            v-else
            class="ar-nav-item"
            :class="{ active: isLeafActive(root) }"
            @click="switchPage(root)"
          >
            <div class="ar-nav-dot" :class="{ on: isLeafActive(root) }"></div>
            <FileIcon size="16" class="ar-nav-icon" />
            <span class="ar-nav-text">{{ root.name }}</span>
          </div>
        </template>

        <div v-if="menuTree.length === 0 && !loading" class="ar-nav-empty">
          <FileIcon size="20" />
          <p>暂无菜单</p>
        </div>
      </nav>

      <!-- 底部操作 -->
      <div class="ar-sidebar-footer">
        <t-button variant="outline" block theme="default" @click="goHome">
          <template #icon><HomeIcon size="16" /></template>
          返回主页
        </t-button>
      </div>
    </aside>

    <!-- ===== 主内容区 ===== -->
    <div class="ar-main">
      <!-- 顶部栏 -->
      <header class="ar-topbar">
        <div class="ar-topbar-left">
          <ComponentBreadcrumbIcon size="18" />
          <span class="ar-breadcrumb-app" @click="clearActive">{{ app?.name }}</span>
          <template v-if="activePage">
            <ChevronRightIcon size="14" class="ar-breadcrumb-sep" />
            <span class="ar-breadcrumb-page">{{ activePage.name }}</span>
          </template>
        </div>
        <div class="ar-topbar-right">
          <span class="ar-time">{{ currentTime }}</span>
        </div>
      </header>

      <!-- 页面内容区 -->
      <div class="ar-content">
        <!-- 加载状态 -->
        <div v-if="loading" class="ar-loading">
          <t-loading size="large" text="加载应用..." />
        </div>

        <!-- 首页/欢迎页（未选中页面时） -->
        <div v-else-if="!activePage" class="ar-welcome">
          <div class="ar-welcome-icon" :style="{ background: app?.color || 'linear-gradient(135deg, #667eea, #764ba2)' }">
            <AppIcon size="48" />
          </div>
          <h2>{{ app?.name || '业务应用' }}</h2>
          <p class="ar-welcome-desc">{{ app?.description || '由低代码平台构建的业务系统' }}</p>
          <div class="ar-welcome-stats">
            <div class="ar-stat-item">
              <span class="ar-stat-num">{{ flatLeaves.length }}</span>
              <span class="ar-stat-label">功能页面</span>
            </div>
          </div>
          <div v-if="flatLeaves.length > 0" class="ar-welcome-nav">
            <p>请从左侧菜单选择功能：</p>
            <div class="ar-shortcut-grid">
              <div
                v-for="leaf in flatLeaves"
                :key="`s-${leaf.id || leaf.pageCode}`"
                class="ar-shortcut-card"
                @click="switchPage(leaf)"
              >
                <div class="ar-shortcut-icon">
                  <FileIcon size="20" />
                </div>
                <span>{{ leaf.name }}</span>
                <ChevronRightIcon size="14" class="ar-shortcut-arrow" />
              </div>
            </div>
          </div>
        </div>

        <!-- 页面渲染区 -->
        <div v-else class="ar-page-viewport">
          <SchemaRenderer :elements="pageElements" :enable-events="true">
            <template #empty>
              <div class="ar-empty-page">
                <LayoutIcon size="56" />
                <h3>空白页面</h3>
                <p>该页面尚未添加任何组件，请前往页面设计器编辑</p>
              </div>
            </template>
          </SchemaRenderer>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { businessAppApi, type BusinessApp } from '../../../api/lowcode/businessApp';
import { pageSchemaApi } from '../../../api/lowcode/pageSchema';
import { appMenuApi, type MenuNode } from '../../../api/lowcode/appMenu';
import SchemaRenderer from '../../../components/lowcode/SchemaRenderer.vue';
import {
  AppIcon, FileIcon, FolderIcon, ChevronRightIcon, HomeIcon,
  ComponentBreadcrumbIcon, LayoutIcon,
} from 'tdesign-icons-vue-next';

const route = useRoute();
const router = useRouter();

const appCode = computed(() => (route.params.appCode as string) || '');
const app = ref<BusinessApp | null>(null);

/** 菜单树（active release）；空时回退到 businessApp.getPages 扁平列表 */
const menuTree = ref<MenuNode[]>([]);

/** 当前打开的页面节点（叶子节点） */
const activePage = ref<MenuNode | null>(null);
const pageElements = ref<any[]>([]);
const loading = ref(true);

/** 分组节点折叠状态 key=节点 id 字符串 */
const collapsedGroups = reactive<Record<string, boolean>>({});

/** 展开成一维列表的所有叶子，用于欢迎页快捷入口和深链匹配 */
const flatLeaves = computed<MenuNode[]>(() => {
  const out: MenuNode[] = [];
  const walk = (nodes: MenuNode[]) => {
    for (const n of nodes) {
      if (n.children && n.children.length > 0) walk(n.children);
      else if (n.pageCode) out.push(n);
    }
  };
  walk(menuTree.value);
  return out;
});

// ===== 当前时间 =====
const currentTime = ref('');
let timeTimer: ReturnType<typeof setInterval> | null = null;

function updateTime() {
  const now = new Date();
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function toggleGroup(node: MenuNode) {
  const key = String(node.id);
  collapsedGroups[key] = !collapsedGroups[key];
}

function isLeafActive(leaf: MenuNode): boolean {
  if (!activePage.value) return false;
  // 优先用 id 比对（菜单树带 id），降级到 pageCode
  if (activePage.value.id && leaf.id) return activePage.value.id === leaf.id;
  return activePage.value.pageCode === leaf.pageCode;
}

// ===== 加载数据 =====
async function loadData() {
  loading.value = true;
  try {
    // 1) 应用信息
    const appRes = await businessAppApi.getByCode(appCode.value);
    app.value = (appRes.data as any)?.data || appRes.data;

    // 2) 菜单树（active release）
    let tree: MenuNode[] = [];
    try {
      const menuRes = await appMenuApi.getActive(appCode.value);
      tree = (menuRes.data as any)?.data || [];
    } catch {
      tree = [];
    }

    // 3) 菜单为空（无 active release）则回退到旧的扁平 pages 列表，保持兼容
    if (!Array.isArray(tree) || tree.length === 0) {
      try {
        const pageRes = await businessAppApi.getPages(appCode.value);
        const pages = ((pageRes.data as any)?.data || pageRes.data || []) as any[];
        tree = (Array.isArray(pages) ? pages : []).map((p: any) => ({
          id: p.id,
          name: p.name,
          pageCode: p.pageCode || p.code,
          menuType: 'menu' as const,
          visible: true,
        }));
      } catch {
        tree = [];
      }
    }

    menuTree.value = tree;

    // 4) URL 带 pageCode 时自动打开对应页面（深链）
    const urlPageCode = (route.params.pageCode as string) || '';
    if (urlPageCode) {
      const leaf = flatLeaves.value.find(l => l.pageCode === urlPageCode);
      if (leaf) {
        await openLeaf(leaf, false);
      }
    }
  } catch {
    app.value = null;
    menuTree.value = [];
  } finally {
    loading.value = false;
  }
}

/** 切到指定叶子并把 pageCode 同步到 URL（pushUrl=true） */
async function switchPage(leaf: MenuNode) {
  if (!leaf.pageCode) {
    // 分组节点点击不导航
    return;
  }
  await openLeaf(leaf, true);
}

/** 真正加载并渲染页面 */
async function openLeaf(leaf: MenuNode, pushUrl: boolean) {
  activePage.value = leaf;
  pageElements.value = [];

  if (pushUrl && route.params.pageCode !== leaf.pageCode) {
    router.replace({
      name: 'AppRuntime',
      params: { appCode: appCode.value, pageCode: leaf.pageCode },
    }).catch(() => {});
  }

  try {
    const res = await pageSchemaApi.getByPageCode(leaf.pageCode!);
    const data = (res.data as any)?.data ?? res.data;
    if (data && data.layoutJson !== undefined) {
      try {
        const layout = typeof data.layoutJson === 'string'
          ? JSON.parse(data.layoutJson)
          : (data.layoutJson || []);
        pageElements.value = Array.isArray(layout) ? layout : [];
      } catch {
        pageElements.value = [];
      }
    }
  } catch {
    pageElements.value = [];
  }
}

function clearActive() {
  activePage.value = null;
  pageElements.value = [];
  // 回欢迎页时把 URL 也清回 /run/:appCode
  if (route.params.pageCode) {
    router.replace({
      name: 'AppRuntime',
      params: { appCode: appCode.value },
    }).catch(() => {});
  }
}

function goHome() {
  router.push('/home');
}

// 当 URL 上 pageCode 变化时（前进后退 / 外部跳转）同步内部状态
watch(
  () => route.params.pageCode,
  (next, prev) => {
    if (next === prev) return;
    if (!next) {
      activePage.value = null;
      pageElements.value = [];
      return;
    }
    const leaf = flatLeaves.value.find(l => l.pageCode === next);
    if (leaf) {
      openLeaf(leaf, false);
    }
  },
);

onMounted(() => {
  loadData();
  updateTime();
  timeTimer = setInterval(updateTime, 30000);
});

onUnmounted(() => {
  if (timeTimer) clearInterval(timeTimer);
});
</script>

<style scoped lang="less">
// ===== 整体布局 =====
.app-runtime {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: #f0f2f5;
}

// ===== 左侧导航栏 =====
.ar-sidebar {
  width: 240px;
  min-width: 240px;
  height: 100vh;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.04);
  z-index: 10;
}

.ar-sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 18px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.ar-brand-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.ar-brand-info {
  flex: 1;
  min-width: 0;
  h3 {
    font-size: 16px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

// 导航菜单
.ar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 12px 8px;
}

.ar-nav-label {
  font-size: 11px;
  font-weight: 600;
  color: #bbb;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 8px 12px 6px;
}

// 分组（带 children 的一级节点）
.ar-nav-group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  margin: 2px 0;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
  color: #1a1a2e;
  font-size: 14px;
  font-weight: 600;

  &:hover {
    background: #f5f7fa;
  }
}

.ar-nav-arrow {
  color: #8c8c8c;
  transition: transform 0.18s;
  flex-shrink: 0;

  &.expanded {
    transform: rotate(90deg);
  }
}

.ar-nav-children {
  padding-left: 8px;
}

// 叶子节点
.ar-nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  margin: 2px 0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
  color: #595959;
  font-size: 14px;
  position: relative;

  &:hover {
    background: #f5f7fa;
    color: #1a1a2e;
  }

  &.active {
    background: linear-gradient(135deg, #eff6ff, #f0f5ff);
    color: #3b82f6;
    font-weight: 600;

    .ar-nav-icon {
      color: #3b82f6;
    }
  }
}

.ar-nav-item-sub {
  padding-left: 24px;
  font-size: 13px;
}

.ar-nav-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #d9d9d9;
  flex-shrink: 0;
  transition: all 0.2s;

  &.on {
    background: #3b82f6;
    box-shadow: 0 0 6px rgba(59, 130, 246, 0.4);
  }
}

.ar-nav-icon {
  flex-shrink: 0;
  color: #8c8c8c;
}

.ar-nav-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ar-nav-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 32px 16px;
  color: #ccc;
  p { font-size: 13px; color: #999; margin: 0; }
}

// 底部
.ar-sidebar-footer {
  padding: 12px;
  border-top: 1px solid #f0f0f0;
}

// ===== 主内容区 =====
.ar-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 顶部栏
.ar-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  height: 52px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.ar-topbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8c8c8c;
}

.ar-breadcrumb-app {
  font-size: 14px;
  color: #8c8c8c;
  cursor: pointer;

  &:hover {
    color: #3b82f6;
  }
}

.ar-breadcrumb-sep {
  color: #d9d9d9;
}

.ar-breadcrumb-page {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.ar-topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.ar-time {
  font-size: 12px;
  color: #bbb;
  font-variant-numeric: tabular-nums;
}

// 内容区
.ar-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.ar-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

// 欢迎页
.ar-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  text-align: center;
}

.ar-welcome-icon {
  width: 88px;
  height: 88px;
  border-radius: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.ar-welcome h2 {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.ar-welcome-desc {
  font-size: 15px;
  color: #8c8c8c;
  margin: 0 0 32px;
}

.ar-welcome-stats {
  display: flex;
  gap: 40px;
  margin-bottom: 36px;
}

.ar-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.ar-stat-num {
  font-size: 32px;
  font-weight: 700;
  color: #3b82f6;
  line-height: 1;
}

.ar-stat-label {
  font-size: 13px;
  color: #8c8c8c;
}

.ar-welcome-nav {
  p {
    font-size: 14px;
    color: #999;
    margin: 0 0 16px;
  }
}

.ar-shortcut-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 10px;
  max-width: 600px;
}

.ar-shortcut-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  color: #595959;

  &:hover {
    border-color: #3b82f6;
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.08);
    color: #3b82f6;
    transform: translateY(-1px);
  }
}

.ar-shortcut-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
  flex-shrink: 0;
}

.ar-shortcut-arrow {
  color: #d9d9d9;
  margin-left: auto;
}

// 页面渲染区
.ar-page-viewport {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  min-height: 500px;
  padding: 24px;
}

.ar-empty-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #ccc;
  gap: 8px;

  h3 { font-size: 18px; color: #aaa; margin: 0; }
  p { font-size: 14px; color: #bbb; margin: 0 0 12px; }
}
</style>
