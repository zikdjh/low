<template>
  <aside class="layout-aside" :class="{ collapsed: settingStore.isSidebarCollapsed }">
    <!-- Logo区域 -->
    <div class="sidebar-header">
      <div class="logo-wrapper">
        <div class="logo-icon">
          <svg viewBox="0 0 40 40" fill="none" class="logo-svg">
            <rect width="40" height="40" rx="10" fill="currentColor" fill-opacity="0.15"/>
            <path d="M12 14l8-4 8 4-8 4-8-4z" fill="currentColor" fill-opacity="0.7"/>
            <path d="M12 20l8 4 8-4M12 26l8 4 8-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-opacity="0.7"/>
          </svg>
        </div>
        <span v-show="!settingStore.isSidebarCollapsed" class="logo-title">
          <span class="title-text">LowCode</span>
          <span class="title-sub">低代码开发平台</span>
        </span>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div v-show="!settingStore.isSidebarCollapsed" class="quick-actions">
      <div class="section-label">快捷操作</div>
      <div class="quick-list">
        <t-button
          v-for="action in quickActions"
          :key="action.name"
          variant="outline"
          size="small"
          class="quick-btn"
          @click="handleQuickAction(action.name)"
        >
          <template #icon><component :is="action.icon" /></template>
          {{ action.label }}
        </t-button>
      </div>
    </div>

    <!-- 导航菜单 -->
    <nav class="sidebar-nav">
      <div v-show="!settingStore.isSidebarCollapsed" class="section-label">导航菜单</div>
      <t-menu
        :items="menuItems"
        :active-name="activeMenu"
        :collapsed="settingStore.isSidebarCollapsed"
        mode="vertical"
        theme="light"
        @click="handleMenuClick"
        class="main-menu"
      />
    </nav>

    <!-- 底部信息 -->
    <div class="sidebar-footer">
      <div v-if="!settingStore.isSidebarCollapsed" class="footer-content">
        <div class="version-info">
          <span class="version-dot"></span>
          <span class="version-text">v2.0.0</span>
        </div>
        <div class="footer-links">
          <t-button variant="text" class="footer-link" @click="openDocs">
            <template #icon><FileTxtIcon /></template>
            文档
          </t-button>
          <t-button variant="text" class="footer-link" @click="openHelp">
            <template #icon><HelpCircleIcon /></template>
            帮助
          </t-button>
        </div>
      </div>
      <div v-else class="footer-collapsed">
        <t-tooltip content="文档" placement="right">
          <t-button variant="text" class="footer-btn" @click="openDocs">
            <FileTxtIcon size="16" />
          </t-button>
        </t-tooltip>
        <t-tooltip content="帮助" placement="right">
          <t-button variant="text" class="footer-btn" @click="openHelp">
            <HelpCircleIcon size="16" />
          </t-button>
        </t-tooltip>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  PlusIcon, UploadIcon, DownloadIcon,
  FileTxtIcon, HelpCircleIcon
} from 'tdesign-icons-vue-next';
import { useSettingStore } from '../../store';
import { homepageRouterList, lowcodeRouterList } from '../../router';

const route = useRoute();
const router = useRouter();
const settingStore = useSettingStore();

const allRoutes = [...homepageRouterList, ...lowcodeRouterList];

const quickActions = [
  { name: 'new', label: '新建页面', icon: PlusIcon },
  { name: 'import', label: '导入', icon: UploadIcon },
  { name: 'export', label: '导出', icon: DownloadIcon }
];

const menuItems = computed(() => {
  const items: any[] = [];
  allRoutes.forEach(r => {
    if (!r.meta?.hidden && r.path !== '/') {
      const routePath = r.path;
      const children = r.children?.filter(c => !c.meta?.hidden).map(c => ({
        label: (c.meta?.title as any)?.zh_CN || c.name,
        name: `${routePath}/${c.path}`,
        path: `${routePath}/${c.path}`,
        icon: c.meta?.icon,
      })) || [];

      items.push({
        label: (r.meta?.title as any)?.zh_CN || r.name,
        name: children.length > 0 ? routePath : `${routePath}${r.children?.[0]?.path || ''}`,
        path: routePath,
        icon: r.meta?.icon,
        children: children.length > 0 ? children : undefined,
      });
    }
  });
  return items;
});

const activeMenu = computed(() => route.path);

function handleMenuClick(name: string) { router.push(name); }
function handleQuickAction(name: string) {
  switch (name) {
    case 'new': router.push('/lowcode/page/list'); break;
    case 'import': router.push('/lowcode/entity'); break;
    case 'export': router.push('/lowcode/entity'); break;
  }
}
function openDocs() { window.open('#', '_blank'); }
function openHelp() { window.open('#', '_blank'); }
</script>

<style scoped lang="less">
.layout-aside {
  width: 260px;
  background: #fff;
  border-right: 1px solid #f0f0f0;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  &.collapsed {
    width: 72px;
  }
}

/* ===== Logo ===== */
.sidebar-header {
  padding: 20px 20px 16px;
}
.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}
.logo-icon {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  color: var(--td-brand-color, #E8A317);
  display: flex;
  align-items: center;
  justify-content: center;
  .logo-svg {
    width: 40px;
    height: 40px;
  }
}
.logo-title {
  display: flex;
  flex-direction: column;
  gap: 1px;
  overflow: hidden;
  white-space: nowrap;
}
.title-text {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: -0.3px;
}
.title-sub {
  font-size: 11px;
  color: #999;
  letter-spacing: 0.5px;
}

/* ===== 分组标签 ===== */
.section-label {
  font-size: 11px;
  font-weight: 600;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 1px;
  padding: 0 20px;
  margin-bottom: 8px;
}

/* ===== 快捷操作 ===== */
.quick-actions {
  padding: 8px 16px 16px;
  border-bottom: 1px solid #f5f5f5;
}
.quick-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.quick-btn {
  justify-content: flex-start;
  border-radius: 8px;
  color: #555;
  border-color: #eee;
  font-size: 13px;
  transition: all 0.2s;
  &:hover {
    color: var(--td-brand-color, #E8A317);
    border-color: var(--td-brand-color-3, #fde68a);
    background: var(--td-brand-color-1, #fffbeb);
  }
}

/* ===== 导航菜单 ===== */
.sidebar-nav {
  flex: 1;
  padding: 12px 12px;
  overflow-y: auto;

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb {
    background: #e0e0e0;
    border-radius: 4px;
    &:hover { background: #ccc; }
  }
}

.main-menu {
  background: transparent;
  border: none;

  :deep(.t-menu-group) { margin-bottom: 4px; }
  :deep(.t-menu-group-title) {
    padding: 8px 12px !important;
    font-size: 11px;
    font-weight: 600;
    color: #999;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
  :deep(.t-menu-item) {
    margin: 2px 0;
    border-radius: 8px;
    color: #555;
    font-size: 14px;
    transition: all 0.2s;

    &:hover {
      background: #f9fafb;
      color: var(--td-brand-color, #E8A317);
    }
    &.t-menu-item--active {
      background: var(--td-brand-color-1, #fffbeb);
      color: var(--td-brand-color, #E8A317);
      font-weight: 600;
    }
  }
  :deep(.t-menu-item__icon) {
    color: inherit;
    font-size: 18px;
  }
  :deep(.t-menu-item__label) { color: inherit; }
  :deep(.t-menu-item__arrow) { color: #bbb; }
  :deep(.t-menu-item--active .t-menu-item__arrow) {
    color: var(--td-brand-color, #E8A317);
  }

  // 折叠时菜单项居中
  :deep(.t-menu--collapsed .t-menu-item) {
    justify-content: center;
    padding: 12px 0;
    .t-menu-item__icon { margin-right: 0 !important; }
  }
}

/* ===== 底部 ===== */
.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid #f5f5f5;
}
.footer-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.version-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.version-dot {
  width: 7px;
  height: 7px;
  background: #52c41a;
  border-radius: 50%;
  box-shadow: 0 0 0 3px rgba(82, 196, 26, 0.15);
}
.version-text {
  font-size: 12px;
  color: #999;
  font-weight: 500;
}
.footer-links {
  display: flex;
  gap: 4px;
}
.footer-link {
  padding: 6px 10px;
  border-radius: 6px;
  color: #999;
  font-size: 12px;
  &:hover { color: #333; background: #f5f5f5; }
}

.footer-collapsed {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.footer-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  color: #999;
  &:hover { background: #f5f5f5; color: #333; }
}
</style>
