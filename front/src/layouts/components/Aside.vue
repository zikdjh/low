<template>
  <aside class="layout-aside" :class="{ collapsed: settingStore.isSidebarCollapsed }">
    <!-- Logo区域 -->
    <div class="sidebar-header">
      <div class="logo-wrapper">
        <div class="logo-icon">
          <CodeIcon />
        </div>
        <span class="logo-title" :class="{ hidden: settingStore.isSidebarCollapsed }">
          <span class="title-text">低代码平台</span>
          <span class="title-sub">Low-Code Platform</span>
        </span>
      </div>
    </div>
    
    <!-- 快捷操作 -->
    <div class="quick-actions" :class="{ hidden: settingStore.isSidebarCollapsed }">
      <div class="quick-title">快捷操作</div>
      <div class="quick-list">
        <t-button 
          v-for="action in quickActions" 
          :key="action.name"
          variant="text" 
          class="quick-btn"
          @click="handleQuickAction(action.name)"
        >
          <template #icon><component :is="action.icon" /></template>
          <span>{{ action.label }}</span>
        </t-button>
      </div>
    </div>
    
    <!-- 导航菜单 -->
    <nav class="sidebar-nav">
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
          <span class="version-label">版本</span>
          <span class="version-number">v2.0.0</span>
        </div>
        <div class="footer-links">
          <t-button variant="text" class="footer-link" @click="openDocs">
            <template #icon><FileTxtIcon /></template>
            <span>文档</span>
          </t-button>
          <t-button variant="text" class="footer-link" @click="openHelp">
            <template #icon><HelpCircleIcon /></template>
            <span>帮助</span>
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
  CodeIcon, PlusIcon, UploadIcon, DownloadIcon, 
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
      
      const hasChildren = children.length > 0;
      
      items.push({
        label: (r.meta?.title as any)?.zh_CN || r.name,
        name: hasChildren ? routePath : `${routePath}${r.children?.[0]?.path || ''}`,
        path: routePath,
        icon: r.meta?.icon,
        children: hasChildren ? children : undefined,
      });
    }
  });
  
  return items;
});

const activeMenu = computed(() => {
  return route.path;
});

function handleMenuClick(name: string) {
  router.push(name);
}

function handleQuickAction(name: string) {
  switch (name) {
    case 'new':
      router.push('/lowcode/page');
      break;
    case 'import':
      console.log('导入功能');
      break;
    case 'export':
      console.log('导出功能');
      break;
  }
}

function openDocs() {
  window.open('#', '_blank');
}

function openHelp() {
  window.open('#', '_blank');
}
</script>

<style scoped lang="less">
.layout-aside {
  width: 250px;
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  color: #fff;
  transition: width 0.3s, all 0.3s;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  
  &.collapsed {
    width: 72px;
  }
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  
  :deep(.t-icon) {
    color: #fff;
    font-size: 22px;
  }
}

.logo-title {
  display: flex;
  flex-direction: column;
  gap: 2px;
  
  &.hidden {
    display: none;
  }
}

.title-text {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.title-sub {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  letter-spacing: 0.5px;
}

.quick-actions {
  padding: 16px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  
  &.hidden {
    display: none;
  }
}

.quick-title {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 10px;
  padding-left: 8px;
}

.quick-list {
  display: flex;
  gap: 8px;
}

.quick-btn {
  flex: 1;
  padding: 10px 8px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  
  :deep(.t-icon) {
    margin-right: 4px;
    font-size: 14px;
  }
  
  &:hover {
    background: rgba(102, 126, 234, 0.2);
    color: #fff;
  }
}

.sidebar-nav {
  flex: 1;
  padding: 16px 0;
  overflow-y: auto;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 3px;
    
    &:hover {
      background: rgba(255, 255, 255, 0.2);
    }
  }
}

.main-menu {
  background: transparent;
  
  :deep(.t-menu-group) {
    margin-bottom: 8px;
  }
  
  :deep(.t-menu-group-title) {
    padding: 8px 20px !important;
    font-size: 11px;
    color: rgba(255, 255, 255, 0.4);
    text-transform: uppercase;
    letter-spacing: 1px;
  }
  
  :deep(.t-menu-item) {
    margin: 2px 8px;
    border-radius: 8px;
    color: rgba(255, 255, 255, 0.75);
    transition: all 0.2s;
    
    &:hover {
      background: rgba(255, 255, 255, 0.08);
      color: #fff;
    }
    
    &.t-menu-item--active {
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.3) 0%, rgba(118, 75, 162, 0.3) 100%);
      color: #fff;
      box-shadow: 0 4px 15px rgba(102, 126, 234, 0.2);
    }
  }
  
  :deep(.t-menu-item__icon) {
    color: inherit;
    font-size: 16px;
  }
  
  :deep(.t-menu-item__label) {
    color: inherit;
    font-size: 14px;
  }
  
  :deep(.t-menu-item__arrow) {
    color: rgba(255, 255, 255, 0.4);
  }
  
  :deep(.t-menu-item--active .t-menu-item__arrow) {
    color: #fff;
  }
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(0, 0, 0, 0.2);
}

.footer-content {
  &.hidden {
    display: none;
  }
}

.version-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-left: 4px;
}

.version-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
}

.version-number {
  font-size: 12px;
  color: #667eea;
  font-weight: 500;
}

.footer-links {
  display: flex;
  gap: 12px;
}

.footer-link {
  padding: 8px 12px;
  border-radius: 6px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
  
  :deep(.t-icon) {
    margin-right: 6px;
    font-size: 12px;
  }
  
  &:hover {
    background: rgba(255, 255, 255, 0.08);
    color: #fff;
  }
}

.footer-collapsed {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.footer-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.6);
  
  &:hover {
    background: rgba(255, 255, 255, 0.1);
    color: #fff;
  }
}
</style>