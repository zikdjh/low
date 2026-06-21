<template>
  <div class="settings-page">
    <div class="page-header">
      <BackButton to="/home" label="返回首页" class="settings-back-btn" />
      <div class="header-text">
        <h2>系统设置</h2>
        <p>自定义您的平台体验</p>
      </div>
    </div>

    <div class="settings-grid">
      <!-- 主题设置 -->
      <div class="setting-card">
        <div class="card-header">
          <div class="card-header-left">
            <div class="card-icon" style="background: linear-gradient(135deg, #f5a623, #e8a317);">
              <SunnyIcon size="18" color="#fff" />
            </div>
            <div>
              <h3>主题设置</h3>
              <p>选择您喜欢的颜色和显示模式</p>
            </div>
          </div>
        </div>
        <div class="card-body">
          <div class="setting-group">
            <label>主题模式</label>
            <div class="mode-selector">
              <div
                v-for="opt in modeOptions"
                :key="opt.value"
                class="mode-card"
                :class="{ active: settingStore.mode === opt.value }"
                @click="handleModeChange(opt.value)"
              >
                <component :is="opt.icon" size="28" />
                <span>{{ opt.label }}</span>
                <CheckCircleIcon v-if="settingStore.mode === opt.value" class="selected-check" size="18" />
              </div>
            </div>
          </div>
          <div class="setting-group">
            <label>主题色</label>
            <div class="theme-colors">
              <div
                v-for="c in colorOptions"
                :key="c"
                class="color-swatch"
                :class="{ active: settingStore.brandTheme === c }"
                :style="{ background: c }"
                @click="handleColorChange(c)"
              >
                <CheckCircleIcon v-if="settingStore.brandTheme === c" size="14" color="#fff" />
              </div>
              <div class="color-swatch custom" @click="handleCustomColor" title="自定义颜色">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10" />
                  <path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 布局偏好 -->
      <div class="setting-card">
        <div class="card-header">
          <div class="card-header-left">
            <div class="card-icon" style="background: linear-gradient(135deg, #6366f1, #8b5cf6);">
              <LayoutIcon size="18" color="#fff" />
            </div>
            <div>
              <h3>布局偏好</h3>
              <p>调整侧边栏和导航设置</p>
            </div>
          </div>
        </div>
        <div class="card-body">
          <div class="setting-row">
            <div class="row-info">
              <span>侧边栏折叠</span>
              <small>折叠后可获得更大的工作区域</small>
            </div>
            <t-switch v-model="sidebarCollapsed" @change="settingStore.toggleSidebar()" />
          </div>
          <div class="setting-row">
            <div class="row-info">
              <span>固定侧边栏</span>
              <small>滚动时侧边栏保持可见</small>
            </div>
            <t-switch :value="settingStore.isSidebarFixed" @change="(v: boolean) => settingStore.updateConfig({ isSidebarFixed: v })" />
          </div>
          <div class="setting-row">
            <div class="row-info">
              <span>固定顶栏</span>
              <small>滚动时顶栏保持可见</small>
            </div>
            <t-switch :value="settingStore.isHeaderFixed" @change="(v: boolean) => settingStore.updateConfig({ isHeaderFixed: v })" />
          </div>
        </div>
      </div>

      <!-- 通知设置 -->
      <div class="setting-card">
        <div class="card-header">
          <div class="card-header-left">
            <div class="card-icon" style="background: linear-gradient(135deg, #10b981, #34d399);">
              <NotificationIcon size="18" color="#fff" />
            </div>
            <div>
              <h3>通知偏好</h3>
              <p>管理消息通知方式</p>
            </div>
          </div>
        </div>
        <div class="card-body">
          <div class="setting-row">
            <div class="row-info">
              <span>系统通知</span>
              <small>接收系统更新和维护通知</small>
            </div>
            <t-switch :value="notifPrefs.system" @change="notifPrefs.system = $event" />
          </div>
          <div class="setting-row">
            <div class="row-info">
              <span>任务通知</span>
              <small>接收任务完成和状态变更通知</small>
            </div>
            <t-switch :value="notifPrefs.task" @change="notifPrefs.task = $event" />
          </div>
          <div class="setting-row">
            <div class="row-info">
              <span>团队协作</span>
              <small>接收团队协作相关消息</small>
            </div>
            <t-switch :value="notifPrefs.team" @change="notifPrefs.team = $event" />
          </div>
          <div class="setting-row">
            <div class="row-info">
              <span>桌面通知</span>
              <small>浏览器桌面推送通知</small>
            </div>
            <t-switch :value="notifPrefs.desktop" @change="notifPrefs.desktop = $event" />
          </div>
        </div>
      </div>

      <!-- 账户信息 -->
      <div class="setting-card">
        <div class="card-header">
          <div class="card-header-left">
            <div class="card-icon" style="background: linear-gradient(135deg, #1677ff, #69b1ff);">
              <UserCircleIcon size="18" color="#fff" />
            </div>
            <div>
              <h3>账户信息</h3>
              <p>查看和管理您的账户</p>
            </div>
          </div>
        </div>
        <div class="card-body">
          <div class="account-info">
            <t-avatar size="64px" class="account-avatar">
              <template #icon><UserIcon size="28" /></template>
            </t-avatar>
            <div class="account-meta">
              <div class="account-name">{{ userStore.displayName }}</div>
              <div class="account-role">系统管理员</div>
              <div class="account-id">UID: admin-001</div>
            </div>
          </div>
          <div class="account-stats">
            <div class="stat-item">
              <span class="stat-value">管理员</span>
              <span class="stat-label">当前角色</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">已认证</span>
              <span class="stat-label">账户状态</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">v2.0.0</span>
              <span class="stat-label">平台版本</span>
            </div>
          </div>
          <div class="account-actions">
            <t-button variant="outline" @click="openChangePasswordModal">修改密码</t-button>
            <t-button variant="outline" theme="danger" @click="handleLogout">退出登录</t-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <t-dialog
      v-model:visible="showPasswordModal"
      header="修改密码"
      :footer="false"
      width="480px"
    >
      <form class="password-form" @submit.prevent="handleChangePassword">
        <div class="form-group">
          <label class="form-label">当前密码</label>
          <t-input
            v-model="passwordForm.currentPassword"
            type="password"
            placeholder="请输入当前密码"
            :status="passwordForm.currentPassword ? 'success' : ''"
          />
        </div>
        <div class="form-group">
          <label class="form-label">新密码</label>
          <t-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（6-32位）"
            :status="passwordForm.newPassword && passwordForm.newPassword.length >= 6 ? 'success' : passwordForm.newPassword ? 'error' : ''"
          />
          <p v-if="passwordForm.newPassword && passwordForm.newPassword.length < 6" class="form-error">密码长度至少6位</p>
        </div>
        <div class="form-group">
          <label class="form-label">确认密码</label>
          <t-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            :status="passwordForm.confirmPassword && passwordForm.confirmPassword === passwordForm.newPassword ? 'success' : passwordForm.confirmPassword ? 'error' : ''"
          />
          <p v-if="passwordForm.confirmPassword && passwordForm.confirmPassword !== passwordForm.newPassword" class="form-error">两次输入的密码不一致</p>
        </div>
        <div class="form-actions">
          <t-button variant="outline" @click="showPasswordModal = false">取消</t-button>
          <t-button theme="primary" type="submit" :loading="isSubmitting">确认修改</t-button>
        </div>
      </form>
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { MessagePlugin } from 'tdesign-vue-next';
import {
  SunnyIcon, MoonIcon, DesktopIcon, CheckCircleIcon,
  LayoutIcon, NotificationIcon, UserCircleIcon, UserIcon
} from 'tdesign-icons-vue-next';
import BackButton from '../../components/common/BackButton.vue';
import { useSettingStore, useUserStore } from '../../store';
import { smoothThemeTransition } from '../../utils/theme';
import { DEFAULT_COLOR_OPTIONS } from '../../config/color';
import userApi from '../../api/user';

const settingStore = useSettingStore();
const userStore = useUserStore();

const sidebarCollapsed = computed({
  get: () => settingStore.isSidebarCollapsed,
  set: () => settingStore.toggleSidebar()
});

const colorOptions = [...DEFAULT_COLOR_OPTIONS, '#E8A317'];

const modeOptions = [
  { value: 'light', label: '浅色', icon: SunnyIcon },
  { value: 'dark', label: '深色', icon: MoonIcon },
  { value: 'auto', label: '跟随系统', icon: DesktopIcon },
];

const notifPrefs = ref({
  system: true,
  task: true,
  team: true,
  desktop: false,
});

// 修改密码相关
const showPasswordModal = ref(false);
const isSubmitting = ref(false);
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
});

function handleModeChange(mode: string) {
  smoothThemeTransition(() => {
    settingStore.updateConfig({ mode: mode as any });
  });
  MessagePlugin.success(`已切换为${modeOptions.find(o => o.value === mode)?.label}模式`);
}

function handleColorChange(color: string) {
  smoothThemeTransition(() => {
    settingStore.updateConfig({ brandTheme: color });
  });
  MessagePlugin.success('主题色已更新');
}

function handleCustomColor() {
  const input = document.createElement('input');
  input.type = 'color';
  input.value = settingStore.brandTheme;
  input.onchange = () => {
    smoothThemeTransition(() => {
      settingStore.updateConfig({ brandTheme: input.value });
    });
    MessagePlugin.success('主题色已更新');
  };
  input.click();
}

function handleLogout() {
  void userStore.logout();
}

function openChangePasswordModal() {
  showPasswordModal.value = true;
  // 重置表单
  passwordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  };
}

async function handleChangePassword() {
  // 表单验证
  if (!passwordForm.value.currentPassword) {
    MessagePlugin.error('请输入当前密码');
    return;
  }
  if (!passwordForm.value.newPassword || passwordForm.value.newPassword.length < 6) {
    MessagePlugin.error('新密码长度至少6位');
    return;
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    MessagePlugin.error('两次输入的密码不一致');
    return;
  }

  isSubmitting.value = true;
  try {
    const result = await userApi.changePassword(
      passwordForm.value.currentPassword,
      passwordForm.value.newPassword,
      passwordForm.value.confirmPassword
    );
    if (result.code === 1) {
      MessagePlugin.success('密码修改成功，请重新登录');
      showPasswordModal.value = false;
      // 密码修改成功后自动登出
      void userStore.logout();
    } else {
      MessagePlugin.error(result.msg || '修改失败');
    }
  } catch (error) {
    MessagePlugin.error('网络异常，请稍后重试');
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<style scoped lang="less">
.settings-page {
  padding: 28px;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 28px;

  .settings-back-btn {
    margin-top: 2px;
  }

  .header-text {
    h2 {
      font-size: 24px;
      font-weight: 700;
      color: #1a1a1a;
      margin: 0 0 6px;
    }
    p {
      font-size: 14px;
      color: #999;
      margin: 0;
    }
  }
}

.settings-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.setting-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  overflow: hidden;
}

.card-header {
  padding: 20px 24px 0;
}
.card-header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.card-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.card-header-left h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 2px;
}
.card-header-left p {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.card-body {
  padding: 20px 24px;
}

.setting-group {
  margin-bottom: 20px;
  &:last-child { margin-bottom: 0; }
  label {
    display: block;
    font-size: 13px;
    font-weight: 600;
    color: #555;
    margin-bottom: 10px;
  }
}

/* 主题模式选择器 */
.mode-selector {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.mode-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  color: #999;
  span { font-size: 13px; font-weight: 500; }
  &:hover { border-color: #e0e0e0; color: #666; }
  &.active {
    border-color: #e8a317;
    background: rgba(245,166,35,0.04);
    color: #e8a317;
  }
}
.selected-check {
  position: absolute;
  top: 6px;
  right: 6px;
  color: #e8a317;
}

/* 主题色选择 */
.theme-colors {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.color-swatch {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid transparent;
  transition: all 0.2s;
  &:hover { transform: scale(1.15); }
  &.active {
    border-color: #e8a317;
    box-shadow: 0 0 0 2px rgba(232,163,23,0.2);
  }
  &.custom {
    background: conic-gradient(from 90deg at 50% 50%, red, orange, yellow, green, cyan, blue, purple, red);
    color: #666;
    &:hover { opacity: 0.9; }
  }
}

/* 开关行 */
.setting-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #fafafa;
  &:last-child { border-bottom: none; }
}
.row-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  span { font-size: 14px; color: #333; font-weight: 500; }
  small { font-size: 12px; color: #999; }
}

/* 账户信息 */
.account-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  margin-bottom: 12px;
  border-bottom: 1px solid #f5f5f5;
}
.account-avatar {
  background: linear-gradient(135deg, #1677ff, #69b1ff) !important;
  flex-shrink: 0;
}
.account-name { font-size: 18px; font-weight: 600; color: #1a1a1a; }
.account-role { font-size: 13px; color: #999; margin-top: 2px; }
.account-id { font-size: 12px; color: #bbb; margin-top: 2px; font-family: monospace; }
.account-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
  margin-bottom: 16px;
}
.stat-item {
  text-align: center;
  .stat-value { display: block; font-size: 14px; font-weight: 600; color: #333; }
  .stat-label { display: block; font-size: 12px; color: #999; margin-top: 2px; }
}
.account-actions {
  display: flex;
  gap: 8px;
}

/* 修改密码弹窗 */
.password-form {
  padding: 16px 0;
}
.form-group {
  margin-bottom: 20px;
}
.form-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #555;
  margin-bottom: 8px;
}
.form-error {
  font-size: 12px;
  color: #f56c6c;
  margin: 6px 0 0;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f5f5f5;
}

/* 响应式 */
@media (max-width: 768px) {
  .settings-page { padding: 16px; }
  .settings-grid { grid-template-columns: 1fr; }
}
</style>
