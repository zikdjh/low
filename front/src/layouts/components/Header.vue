<template>
  <header class="layout-header">
    <div class="header-left">
      <t-button
        variant="text"
        size="large"
        @click="toggleSidebar"
        class="sidebar-toggle"
      >
        <t-icon name="menu" />
      </t-button>
      <div class="logo">
        <t-icon name="code" />
        <span>低代码平台</span>
      </div>
    </div>
    
    <div class="header-center">
      <t-breadcrumb :items="breadcrumbItems" />
    </div>
    
    <div class="header-right">
      <t-space>
        <t-button variant="text" @click="goToSettings">
          <template #icon><t-icon name="settings" /></template>
        </t-button>
        <t-button variant="text" @click="logout">
          <template #icon><t-icon name="logout" /></template>
          退出
        </t-button>
      </t-space>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useSettingStore } from '../../store';

const router = useRouter();
const route = useRoute();
const settingStore = useSettingStore();

const breadcrumbItems = computed(() => {
  const matched = route.matched.filter(r => !r.meta?.hidden);
  return matched.map(r => ({
    label: (r.meta?.title as any)?.zh_CN || r.name,
    path: r.path,
  }));
});

function toggleSidebar() {
  settingStore.toggleSidebar();
}

function goToSettings() {
  router.push('/settings');
}

function logout() {
  sessionStorage.removeItem('access');
  router.push('/login');
}
</script>

<style scoped lang="less">
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.header-center {
  flex: 1;
  padding: 0 40px;
}

.header-right {
  display: flex;
  align-items: center;
}

.sidebar-toggle {
  padding: 8px;
}
</style>
