<template>
  <aside class="layout-aside" :class="{ collapsed: settingStore.isSidebarCollapsed }">
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
import { useSettingStore } from '../../store';
import { homepageRouterList, lowcodeRouterList } from '../../router';

const route = useRoute();
const router = useRouter();
const settingStore = useSettingStore();

const allRoutes = [...homepageRouterList, ...lowcodeRouterList];

const menuItems = computed(() => {
  const items: any[] = [];
  
  allRoutes.forEach(route => {
    if (!route.meta?.hidden) {
      const children = route.children?.filter(c => !c.meta?.hidden).map(c => ({
        label: (c.meta?.title as any)?.zh_CN || c.name,
        name: c.path,
        path: c.path,
      })) || [];
      
      items.push({
        label: (route.meta?.title as any)?.zh_CN || route.name,
        name: route.path,
        path: route.path,
        icon: route.meta?.icon,
        children: children.length > 0 ? children : undefined,
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
  background: #fff;
  border-right: 1px solid #e8e8e8;
  transition: width 0.3s;
  flex-shrink: 0;
  
  &.collapsed {
    width: 64px;
  }
}

.sidebar-nav {
  padding: 16px 0;
}
</style>
