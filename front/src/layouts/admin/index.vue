<template>
  <div class="admin-layout">
    <!-- 顶部导航栏 -->
    <header class="admin-header">
      <div class="admin-header-left">
        <div class="admin-logo">
          <svg viewBox="0 0 36 36" fill="none" class="admin-logo-svg">
            <rect width="36" height="36" rx="9" fill="url(#adminLogoGrad)" />
            <path d="M10 12l8-4 8 4-8 4-8-4z" fill="#fff" opacity="0.85" />
            <path d="M10 18l8 4 8-4M10 24l8 4 8-4" stroke="#fff" stroke-width="1.8" stroke-linecap="round" opacity="0.7" />
            <defs>
              <linearGradient id="adminLogoGrad" x1="0" y1="0" x2="36" y2="36">
                <stop offset="0%" stop-color="#f5a623" />
                <stop offset="100%" stop-color="#d4920a" />
              </linearGradient>
            </defs>
          </svg>
        </div>
        <span class="admin-logo-text">LowCode</span>
        <span class="admin-badge">管理后台</span>
      </div>

      <nav class="admin-nav">
        <router-link to="/admin/users" class="admin-nav-item" active-class="active">
          <UserIcon size="16" />
          <span>用户管理</span>
        </router-link>
        <router-link to="/admin/pages" class="admin-nav-item" active-class="active">
          <LayoutIcon size="16" />
          <span>页面管理</span>
        </router-link>
        <router-link to="/admin/entities" class="admin-nav-item" active-class="active">
          <DataBaseIcon size="16" />
          <span>实体管理</span>
        </router-link>
      </nav>

      <div class="admin-header-right">
        <t-tooltip content="系统设置">
          <t-button variant="text" class="admin-icon-btn" @click="goToProfile">
            <SettingIcon size="18" />
          </t-button>
        </t-tooltip>
        <t-dropdown :options="userMenuOptions" trigger="click" @click="handleUserMenu">
          <t-button variant="text" class="admin-user-btn">
            <t-avatar size="28px">
              <template #icon><UserIcon /></template>
            </t-avatar>
            <span class="admin-user-name">{{ displayName }}</span>
            <ChevronDownIcon size="14" />
          </t-button>
        </t-dropdown>
      </div>
    </header>

    <!-- 内容区 -->
    <main class="admin-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, h } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import {
  UserIcon, LayoutIcon, DataBaseIcon, SettingIcon,
  LogoutIcon, UserCircleIcon, ChevronDownIcon,
} from 'tdesign-icons-vue-next';
import { useUserStore } from '../../store';
import { adminAuthApi } from '../../api/admin';

const router = useRouter();
const userStore = useUserStore();

const displayName = computed(() => userStore.displayName);

const userMenuOptions = [
  { content: '个人资料', value: 'profile', prefixIcon: () => h(UserCircleIcon) },
  { content: '退出登录', value: 'logout', prefixIcon: () => h(LogoutIcon) },
];

function goToProfile() {
  router.push('/admin/profile');
}

async function handleUserMenu(data: { value: string }) {
  if (data.value === 'profile') {
    router.push('/admin/profile');
  } else if (data.value === 'logout') {
    try { await adminAuthApi.logout(); } catch {}
    await userStore.logout(false);
    router.push('/admin/login');
  }
}
</script>

<style scoped lang="less">
.admin-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f5f7fa;
}

/* Header */
.admin-header {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #eef0f2;
  flex-shrink: 0;
  gap: 12px;
}
.admin-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.admin-logo {
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  .admin-logo-svg { width: 32px; height: 32px; }
}
.admin-logo-text {
  font-size: 17px; font-weight: 700; color: #1a1a1a; letter-spacing: -0.3px;
}
.admin-badge {
  font-size: 11px; color: #d4920a; background: #fff3e0;
  padding: 2px 8px; border-radius: 4px; margin-left: 2px;
}

/* Nav */
.admin-nav {
  display: flex; align-items: center; gap: 4px;
  margin-left: 40px; flex: 1;
}
.admin-nav-item {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 14px; border-radius: 7px;
  font-size: 13px; color: #555; text-decoration: none;
  transition: all 0.15s; border: 1px solid transparent;
  &:hover { background: #fff8ed; color: #d4920a; }
  &.active {
    background: #fff3e0; color: #d4920a; font-weight: 600;
    border-color: #f5c842;
  }
}

/* Right */
.admin-header-right {
  display: flex; align-items: center; gap: 4px; flex-shrink: 0;
}
.admin-icon-btn {
  width: 36px; height: 36px; border-radius: 8px; color: #888;
  &:hover { background: #f5f5f5; color: #333; }
}
.admin-user-btn {
  padding: 4px 8px; border-radius: 8px; height: 40px;
  display: flex; align-items: center; gap: 8px;
  &:hover { background: #f5f5f5; }
}
.admin-user-name {
  font-size: 13px; color: #333; font-weight: 500; max-width: 100px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

/* Content */
.admin-content {
  flex: 1; padding: 24px; overflow-y: auto;
  max-width: 1400px; width: 100%; margin: 0 auto;
}
</style>
