<template>
  <aside class="layout-aside" :class="{ collapsed: settingStore.isSidebarCollapsed }">
    <div class="sidebar-header">
      <CodeIcon class="sidebar-logo-icon" />
      <span class="sidebar-title" :class="{ hidden: settingStore.isSidebarCollapsed }">低代码平台</span>
    </div>
    <nav class="sidebar-nav">
      <t-menu
        :items="menuItems"
        :active-name="activeMenu"
        :collapsed="settingStore.isSidebarCollapsed"
        mode="vertical"
        theme="light"
        @click="handleMenuClick"
      />
    </nav>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { CodeIcon } from 'tdesign-icons-vue-next';
import { useSettingStore } from '../../store';
import { homepageRouterList, lowcodeRouterList } from '../../router';

const route = useRoute();
const router = useRouter();
const settingStore = useSettingStore();

const allRoutes = [...homepageRouterList, ...lowcodeRouterList];

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
</script>

<style scoped lang="less">
.layout-aside {
  width: 220px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  color: #fff;
  transition: width 0.3s;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  
  &.collapsed {
    width: 64px;
  }
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  
  .sidebar-logo-icon {
    width: 28px;
    height: 28px;
    color: #667eea;
  }
  
  .sidebar-title {
    font-size: 16px;
    font-weight: 600;
    
    &.hidden {
      display: none;
    }
  }
}

.sidebar-nav {
  flex: 1;
  padding: 16px 0;
  
  :deep(.t-menu) {
    background: transparent;
    
    .t-menu-item {
      color: rgba(255, 255, 255, 0.85);
      
      &:hover {
        background: rgba(255, 255, 255, 0.1);
        color: #fff;
      }
      
      &.t-menu-item--active {
        background: rgba(102, 126, 234, 0.3);
        color: #fff;
      }
    }
    
    .t-menu-item__icon {
      color: inherit;
    }
    
    .t-menu-item__label {
      color: inherit;
    }
    
    .t-menu-group-title {
      color: rgba(255, 255, 255, 0.5);
    }
  }
}
</style>
