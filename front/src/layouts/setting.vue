<template>
  <div class="settings-container">
    <div class="settings-sidebar">
      <div class="sidebar-header">
        <h2>设置</h2>
      </div>
      <div class="sidebar-nav">
        <div 
          v-for="item in menuItems" 
          :key="item.key"
          class="nav-item"
          :class="{ active: activeTab === item.key }"
          @click="activeTab = item.key"
        >
          <component :is="item.icon" />
          <span>{{ item.label }}</span>
        </div>
      </div>
    </div>
    
    <div class="settings-content">
      <!-- 主题设置 -->
      <div v-if="activeTab === 'theme'" class="setting-section">
        <div class="section-header">
          <h3>主题设置</h3>
          <p>自定义您的界面外观</p>
        </div>
        
        <div class="theme-card">
          <h4>主题模式</h4>
          <div class="mode-options">
            <div 
              v-for="mode in modeOptions" 
              :key="mode.value"
              class="mode-option"
              :class="{ active: settingStore.mode === mode.value }"
              @click="changeMode(mode.value)"
            >
              <component :is="mode.icon" />
              <span>{{ mode.label }}</span>
            </div>
          </div>
        </div>
        
        <div class="theme-card">
          <h4>主题色</h4>
          <div class="color-options">
            <div 
              v-for="color in colorOptions" 
              :key="color"
              class="color-option"
              :class="{ active: settingStore.brandTheme === color }"
              :style="{ background: color }"
              @click="changeThemeColor(color)"
            >
              <CheckIcon v-if="settingStore.brandTheme === color" />
            </div>
          </div>
          <div class="custom-color">
            <span>自定义颜色</span>
            <div class="color-picker-wrapper">
              <input 
                type="color" 
                v-model="customColor"
                @change="changeCustomColor"
                class="color-picker"
              />
              <span class="color-value">{{ customColor }}</span>
            </div>
          </div>
        </div>
        
        <div class="theme-card">
          <h4>侧边栏模式</h4>
          <div class="mode-options">
            <div 
              v-for="mode in sideModeOptions" 
              :key="mode.value"
              class="mode-option"
              :class="{ active: settingStore.sideMode === mode.value }"
              @click="changeSideMode(mode.value)"
            >
              <component :is="mode.icon" />
              <span>{{ mode.label }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 通知设置 -->
      <div v-if="activeTab === 'notification'" class="setting-section">
        <div class="section-header">
          <h3>通知设置</h3>
          <p>管理您的通知偏好</p>
        </div>
        
        <div class="notification-card">
          <div class="notification-item">
            <div class="notification-info">
              <h4>系统通知</h4>
              <p>接收系统更新、维护提醒等通知</p>
            </div>
            <t-switch 
              v-model="notificationSettings.system"
              @change="saveNotificationSettings"
            />
          </div>
          
          <div class="notification-item">
            <div class="notification-info">
              <h4>任务通知</h4>
              <p>接收任务完成、状态变更等通知</p>
            </div>
            <t-switch 
              v-model="notificationSettings.task"
              @change="saveNotificationSettings"
            />
          </div>
          
          <div class="notification-item">
            <div class="notification-info">
              <h4>邮件通知</h4>
              <p>将重要通知发送到您的邮箱</p>
            </div>
            <t-switch 
              v-model="notificationSettings.email"
              @change="saveNotificationSettings"
            />
          </div>
          
          <div class="notification-item">
            <div class="notification-info">
              <h4>声音提醒</h4>
              <p>收到新通知时播放提示音</p>
            </div>
            <t-switch 
              v-model="notificationSettings.sound"
              @change="saveNotificationSettings"
            />
          </div>
        </div>
        
        <div class="notification-card">
          <h4>通知历史</h4>
          <div class="history-list">
            <div 
              v-for="(item, index) in notificationHistory" 
              :key="index"
              class="history-item"
            >
              <div class="history-icon">
                <component :is="item.icon" />
              </div>
              <div class="history-content">
                <p class="history-title">{{ item.title }}</p>
                <p class="history-time">{{ item.time }}</p>
              </div>
              <t-button variant="text" size="small" @click="clearHistory(index)">删除</t-button>
            </div>
          </div>
          <div class="clear-all">
            <t-button variant="text" size="small" @click="clearAllHistory">清空全部</t-button>
          </div>
        </div>
      </div>
      
      <!-- 通用设置 -->
      <div v-if="activeTab === 'general'" class="setting-section">
        <div class="section-header">
          <h3>通用设置</h3>
          <p>配置基本的应用设置</p>
        </div>
        
        <div class="general-card">
          <div class="general-item">
            <div class="general-info">
              <h4>语言</h4>
              <p>应用显示语言</p>
            </div>
            <t-select 
              v-model="generalSettings.language"
              :options="languageOptions"
              @change="saveGeneralSettings"
            />
          </div>
          
          <div class="general-item">
            <div class="general-info">
              <h4>时区</h4>
              <p>选择您所在的时区</p>
            </div>
            <t-select 
              v-model="generalSettings.timezone"
              :options="timezoneOptions"
              @change="saveGeneralSettings"
            />
          </div>
          
          <div class="general-item">
            <div class="general-info">
              <h4>日期格式</h4>
              <p>设置日期显示格式</p>
            </div>
            <t-select 
              v-model="generalSettings.dateFormat"
              :options="dateFormatOptions"
              @change="saveGeneralSettings"
            />
          </div>
        </div>
        
        <div class="general-card">
          <div class="general-item">
            <div class="general-info">
              <h4>开启标签页</h4>
              <p>在顶部显示打开的页面标签</p>
            </div>
            <t-switch 
              v-model="generalSettings.useTabs"
              @change="saveGeneralSettings"
            />
          </div>
          
          <div class="general-item">
            <div class="general-info">
              <h4>固定侧边栏</h4>
              <p>侧边栏始终固定在左侧</p>
            </div>
            <t-switch 
              v-model="settingStore.isSidebarFixed"
              @change="saveGeneralSettings"
            />
          </div>
          
          <div class="general-item">
            <div class="general-info">
              <h4>固定顶部导航</h4>
              <p>顶部导航始终固定在顶部</p>
            </div>
            <t-switch 
              v-model="settingStore.isHeaderFixed"
              @change="saveGeneralSettings"
            />
          </div>
        </div>
      </div>
      
      <!-- 账户设置 -->
      <div v-if="activeTab === 'account'" class="setting-section">
        <div class="section-header">
          <h3>账户设置</h3>
          <p>管理您的账户信息</p>
        </div>
        
        <div class="account-card">
          <div class="avatar-section">
            <div class="avatar">
              <UserIcon />
            </div>
            <div class="avatar-info">
              <h4>{{ accountInfo.name }}</h4>
              <p>{{ accountInfo.email }}</p>
            </div>
            <t-button variant="outline" size="small">更换头像</t-button>
          </div>
        </div>
        
        <div class="account-card">
          <h4>基本信息</h4>
          <div class="form-row">
            <t-input 
              v-model="accountInfo.name" 
              label="用户名"
              placeholder="请输入用户名"
            />
            <t-input 
              v-model="accountInfo.email" 
              label="邮箱"
              placeholder="请输入邮箱"
            />
          </div>
          <div class="form-row">
            <t-input 
              v-model="accountInfo.phone" 
              label="手机号"
              placeholder="请输入手机号"
            />
            <t-select 
              v-model="accountInfo.gender" 
              label="性别"
              :options="genderOptions"
            />
          </div>
          <t-button theme="primary" @click="saveAccountInfo">保存更改</t-button>
        </div>
        
        <div class="account-card">
          <h4>修改密码</h4>
          <div class="form-row">
            <t-input 
              v-model="passwordInfo.oldPassword" 
              label="旧密码"
              type="password"
              placeholder="请输入旧密码"
            />
          </div>
          <div class="form-row">
            <t-input 
              v-model="passwordInfo.newPassword" 
              label="新密码"
              type="password"
              placeholder="请输入新密码"
            />
            <t-input 
              v-model="passwordInfo.confirmPassword" 
              label="确认密码"
              type="password"
              placeholder="请再次输入新密码"
            />
          </div>
          <t-button theme="primary" @click="changePassword">修改密码</t-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { 
  PaletteIcon, NotificationIcon, SettingIcon, UserIcon, 
  SunnyIcon, MoonIcon, CheckIcon
} from 'tdesign-icons-vue-next';
import GlobeIcon from 'tdesign-icons-vue-next';
import { useSettingStore } from '../store';

const settingStore = useSettingStore();

const activeTab = ref('theme');
const customColor = ref('#0052D9');

const menuItems = [
  { key: 'theme', label: '主题', icon: PaletteIcon },
  { key: 'notification', label: '通知', icon: NotificationIcon },
  { key: 'general', label: '通用', icon: SettingIcon },
  { key: 'account', label: '账户', icon: UserIcon },
];

const modeOptions = [
  { value: 'light', label: '浅色', icon: SunnyIcon },
  { value: 'dark', label: '深色', icon: MoonIcon },
  { value: 'auto', label: '跟随系统', icon: GlobeIcon },
];

const sideModeOptions = [
  { value: 'light', label: '浅色', icon: SunnyIcon },
  { value: 'dark', label: '深色', icon: MoonIcon },
];

const colorOptions = [
  '#0052D9', '#0594FA', '#00A870', '#EBB105', 
  '#ED7B2F', '#E34D59', '#ED49B4', '#834EC2'
];

const notificationSettings = reactive({
  system: true,
  task: true,
  email: false,
  sound: true
});

const notificationHistory = ref([
  { title: '系统更新提醒', time: '5分钟前', icon: NotificationIcon },
  { title: '页面发布成功', time: '15分钟前', icon: CheckIcon },
  { title: '浏览器版本警告', time: '1小时前', icon: GlobeIcon },
]);

const generalSettings = reactive({
  language: 'zh-CN',
  timezone: 'Asia/Shanghai',
  dateFormat: 'YYYY-MM-DD',
  useTabs: false
});

const languageOptions = [
  { label: '中文', value: 'zh-CN' },
  { label: 'English', value: 'en-US' },
];

const timezoneOptions = [
  { label: '北京时间 (UTC+8)', value: 'Asia/Shanghai' },
  { label: '东京时间 (UTC+9)', value: 'Asia/Tokyo' },
  { label: '纽约时间 (UTC-5)', value: 'America/New_York' },
];

const dateFormatOptions = [
  { label: 'YYYY-MM-DD', value: 'YYYY-MM-DD' },
  { label: 'DD/MM/YYYY', value: 'DD/MM/YYYY' },
  { label: 'MM/DD/YYYY', value: 'MM/DD/YYYY' },
];

const accountInfo = reactive({
  name: '管理员',
  email: 'admin@example.com',
  phone: '',
  gender: 'male'
});

const genderOptions = [
  { label: '男', value: 'male' },
  { label: '女', value: 'female' },
  { label: '保密', value: 'secret' },
];

const passwordInfo = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

function changeMode(mode: string) {
  settingStore.updateConfig({ mode });
}

function changeSideMode(mode: string) {
  settingStore.updateConfig({ sideMode: mode });
}

function changeThemeColor(color: string) {
  settingStore.updateConfig({ brandTheme: color });
  customColor.value = color;
}

function changeCustomColor() {
  settingStore.updateConfig({ brandTheme: customColor.value });
}

function saveNotificationSettings() {
  localStorage.setItem('notificationSettings', JSON.stringify(notificationSettings));
}

function clearHistory(index: number) {
  notificationHistory.value.splice(index, 1);
}

function clearAllHistory() {
  notificationHistory.value = [];
}

function saveGeneralSettings() {
  localStorage.setItem('generalSettings', JSON.stringify(generalSettings));
}

function saveAccountInfo() {
  console.log('保存账户信息:', accountInfo);
}

function changePassword() {
  console.log('修改密码:', passwordInfo);
}

onMounted(() => {
  customColor.value = settingStore.brandTheme;
});
</script>

<style scoped lang="less">
.settings-container {
  display: flex;
  height: 100vh;
  background: #f5f7fa;
}

.settings-sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid #e8ecf0;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 24px;
  border-bottom: 1px solid #e8ecf0;
  
  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #1a1a1a;
  }
}

.sidebar-nav {
  flex: 1;
  padding: 12px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: #666;
  
  &:hover {
    background: #f5f7fa;
    color: #1a1a1a;
  }
  
  &.active {
    background: #e8f0fe;
    color: #0052D9;
    
    :deep(.t-icon) {
      color: #0052D9;
    }
  }
  
  :deep(.t-icon) {
    font-size: 16px;
  }
}

.settings-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.setting-section {
  max-width: 800px;
}

.section-header {
  margin-bottom: 24px;
  
  h3 {
    margin: 0 0 8px 0;
    font-size: 20px;
    font-weight: 600;
    color: #1a1a1a;
  }
  
  p {
    margin: 0;
    color: #888;
    font-size: 14px;
  }
}

.theme-card, .notification-card, .general-card, .account-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  h4 {
    margin: 0 0 16px 0;
    font-size: 15px;
    font-weight: 600;
    color: #333;
  }
}

.mode-options {
  display: flex;
  gap: 12px;
}

.mode-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  border-radius: 10px;
  border: 2px solid #e8ecf0;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    border-color: #0052D9;
  }
  
  &.active {
    border-color: #0052D9;
    background: #e8f0fe;
  }
  
  :deep(.t-icon) {
    font-size: 24px;
    color: #666;
  }
  
  span {
    font-size: 14px;
    color: #333;
  }
}

.color-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.color-option {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:hover {
    transform: scale(1.1);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
  
  &.active {
    transform: scale(1.15);
    box-shadow: 0 0 0 3px rgba(0, 82, 217, 0.3);
    
    :deep(.t-icon) {
      color: #fff;
      font-size: 18px;
    }
  }
}

.custom-color {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e8ecf0;
  
  span {
    font-size: 14px;
    color: #666;
  }
}

.color-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-picker {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.color-value {
  font-size: 14px;
  color: #0052D9;
  font-family: monospace;
}

.notification-item, .general-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
}

.notification-info, .general-info {
  h4 {
    margin: 0 0 4px 0;
    font-size: 14px;
    font-weight: 500;
    color: #333;
  }
  
  p {
    margin: 0;
    font-size: 13px;
    color: #888;
  }
}

.history-list {
  max-height: 200px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
}

.history-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  
  :deep(.t-icon) {
    color: #666;
  }
}

.history-content {
  flex: 1;
  
  .history-title {
    margin: 0 0 4px 0;
    font-size: 14px;
    color: #333;
  }
  
  .history-time {
    margin: 0;
    font-size: 12px;
    color: #999;
  }
}

.clear-all {
  text-align: right;
  padding-top: 12px;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  
  :deep(.t-input), :deep(.t-select) {
    flex: 1;
  }
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  
  :deep(.t-icon) {
    color: #fff;
    font-size: 32px;
  }
}

.avatar-info {
  flex: 1;
  
  h4 {
    margin: 0 0 4px 0;
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }
  
  p {
    margin: 0;
    font-size: 14px;
    color: #888;
  }
}
</style>
