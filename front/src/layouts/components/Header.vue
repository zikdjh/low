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
      <div class="logo">
        <div class="logo-icon">
          <CodeIcon />
        </div>
        <span class="logo-text">低代码开发平台</span>
      </div>
    </div>
    
    <div class="header-center">
      <div class="search-box">
        <SearchIcon class="search-icon" />
        <input 
          type="text" 
          class="search-input" 
          placeholder="搜索组件、页面..." 
          v-model="searchText"
          @keyup.enter="handleSearch"
        />
        <t-button 
          v-if="searchText" 
          variant="text" 
          class="clear-btn"
          @click="searchText = ''"
        >
          <CloseIcon size="14" />
        </t-button>
      </div>
      <t-breadcrumb :items="breadcrumbItems" class="breadcrumb" />
    </div>
    
    <div class="header-right">
      <t-space :size="16">
        <t-button 
          variant="text" 
          class="icon-btn"
          @click="toggleTheme"
          title="切换主题"
        >
          <component :is="isDarkTheme ? SunnyIcon : MoonIcon" />
        </t-button>
        
        <t-button 
          variant="text" 
          class="icon-btn notification-btn"
          @click="openNotifications"
          title="通知"
        >
          <NotificationIcon />
          <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount }}</span>
        </t-button>
        
        <t-button 
          variant="text" 
          class="icon-btn"
          @click="goToSettings"
          title="设置"
        >
          <SettingIcon />
        </t-button>
        
        <div class="user-info" @click="toggleUserMenu">
          <div class="user-avatar">
            <UserIcon />
          </div>
          <span class="user-name">{{ userName }}</span>
          <ChevronDownIcon class="user-arrow" :class="{ rotated: showUserMenu }" />
        </div>
        
        <t-dropdown 
          v-model="showUserMenu"
          :items="userMenuItems"
          placement="bottom-right"
          @click="handleUserMenuClick"
        />
      </t-space>
    </div>
    
    <!-- 通知面板 -->
    <t-drawer 
      :visible="showNotifications" 
      @visible-change="showNotifications = $event"
      title="通知中心" 
      :width="400"
      placement="right"
      @confirm="handleDrawerConfirm"
      @cancel="handleDrawerCancel"
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
            <div class="notification-icon">
              <component :is="item.icon" />
            </div>
            <div class="notification-content">
              <p class="notification-title">{{ item.title }}</p>
              <p class="notification-desc">{{ item.description }}</p>
              <span class="notification-time">{{ item.time }}</span>
            </div>
          </div>
        </div>
      </div>
    </t-drawer>
  </header>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { 
  MenuIcon, CodeIcon, SettingIcon, SearchIcon, CloseIcon,
  MoonIcon, UserIcon, ChevronDownIcon,
  CheckCircleIcon, InkIcon, ArticleIcon, NotificationIcon, SunnyIcon
} from 'tdesign-icons-vue-next';
import { useSettingStore } from '../../store';

const router = useRouter();
const route = useRoute();
const settingStore = useSettingStore();

const searchText = ref('');
const isDarkTheme = computed(() => settingStore.displayMode === 'dark');
const showUserMenu = ref(false);
const showNotifications = ref(false);
const userName = ref('管理员');
const unreadCount = ref(3);

const notifications = ref([
  {
    title: '系统更新',
    description: '低代码平台已更新至 v2.0.0，新增多项功能',
    time: '5分钟前',
    read: false,
    icon: InkIcon
  },
  {
    title: '任务完成',
    description: '您创建的页面"首页"已成功发布',
    time: '15分钟前',
    read: false,
    icon: CheckCircleIcon
  },
  {
    title: '警告',
    description: '检测到浏览器版本过低，建议升级',
    time: '1小时前',
    read: true,
    icon: ArticleIcon
  }
]);

const breadcrumbItems = computed(() => {
  const matched = route.matched.filter(r => !r.meta?.hidden);
  return matched.map(r => ({
    label: (r.meta?.title as any)?.zh_CN || r.name,
    path: r.path,
  }));
});

const userMenuItems = [
  { label: '个人中心', name: 'profile' },
  { label: '修改密码', name: 'password' },
  { type: 'divider' },
  { label: '退出登录', name: 'logout' }
];

function toggleSidebar() {
  settingStore.toggleSidebar();
}

function toggleTheme() {
  const currentMode = settingStore.mode;
  const nextMode = currentMode === 'light' ? 'dark' : currentMode === 'dark' ? 'auto' : 'light';
  settingStore.updateConfig({ mode: nextMode });
}

function handleSearch() {
  if (searchText.value.trim()) {
    console.log('搜索:', searchText.value);
  }
}

function goToSettings() {
  router.push('/settings');
}

function openNotifications() {
  showNotifications.value = true;
}

function handleDrawerConfirm() {
  markAllRead();
  showNotifications.value = false;
}

function handleDrawerCancel() {
  showNotifications.value = false;
}

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value;
}

function handleUserMenuClick(name: string) {
  if (name === 'logout') {
    sessionStorage.removeItem('access');
    router.push('/login');
  } else {
    console.log('用户菜单:', name);
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
  height: 70px;
  padding: 0 24px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-bottom: 1px solid #e2e8f0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  :deep(.t-icon) {
    color: #fff;
    font-size: 20px;
  }
}

.logo-text {
  font-size: 19px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-center {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 0 40px;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  background: #f1f5f9;
  border-radius: 12px;
  padding: 8px 16px;
  transition: all 0.3s;
  width: 320px;
  
  &:focus-within {
    background: #fff;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    border: 1px solid #667eea;
  }
}

.search-icon {
  color: #94a3b8;
  margin-right: 10px;
  font-size: 16px;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 14px;
  color: #334155;
}

.clear-btn {
  padding: 4px;
  color: #94a3b8;
}

.breadcrumb {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
}

.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:hover {
    background: #f1f5f9;
  }
  
  :deep(.t-icon) {
    font-size: 18px;
    color: #475569;
  }
  
  &:hover :deep(.t-icon) {
    color: #667eea;
  }
}

.notification-btn {
  position: relative;
}

.notification-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  min-width: 18px;
  height: 18px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.3);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.3s;
  
  &:hover {
    background: #f1f5f9;
  }
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  :deep(.t-icon) {
    color: #fff;
    font-size: 16px;
  }
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.user-arrow {
  transition: transform 0.3s;
  
  &.rotated {
    transform: rotate(180deg);
  }
}

.notification-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e2e8f0;
  font-weight: 600;
  color: #1e293b;
}

.notification-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
  
  &:hover {
    background: #f8fafc;
  }
  
  &.unread {
    background: #f0f9ff;
  }
}

.notification-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  
  :deep(.t-icon) {
    color: #667eea;
  }
}

.notification-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notification-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.notification-desc {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.notification-time {
  font-size: 12px;
  color: #94a3b8;
}

.sidebar-toggle {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:hover {
    background: #f1f5f9;
  }
  
  :deep(.t-icon) {
    font-size: 18px;
  }
}
</style>