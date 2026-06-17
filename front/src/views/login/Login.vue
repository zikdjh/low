<template>
  <div class="login-wrapper">
    <!-- 背景装饰 -->
    <div class="bg-layer">
      <div class="bg-circle c1"></div>
      <div class="bg-circle c2"></div>
      <div class="bg-circle c3"></div>
      <div class="bg-dots"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 左侧品牌区 -->
      <div class="brand-panel">
        <div class="brand-inner">
          <div class="brand-logo-wrap">
            <svg viewBox="0 0 60 60" fill="none" class="brand-logo">
              <rect width="60" height="60" rx="14" fill="#fff" fill-opacity="0.2"/>
              <path d="M18 21l12-6 12 6-12 6-12-6z" fill="#fff" opacity="0.85"/>
              <path d="M18 30l12 6 12-6M18 39l12 6 12-6" stroke="#fff" stroke-width="2.5" stroke-linecap="round" opacity="0.7"/>
            </svg>
          </div>
          <h1 class="brand-name">LowCode</h1>
          <p class="brand-tagline">低代码开发平台</p>
          <div class="brand-features">
            <div class="bf-item">
              <CheckCircleFilledIcon size="16" />
              <span>可视化拖拽设计</span>
            </div>
            <div class="bf-item">
              <CheckCircleFilledIcon size="16" />
              <span>一键生成代码</span>
            </div>
            <div class="bf-item">
              <CheckCircleFilledIcon size="16" />
              <span>企业级安全架构</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-panel">
        <div class="form-inner">
          <div class="form-header">
            <h2>{{ isLogin ? '欢迎回来' : '创建账户' }}</h2>
            <p>{{ isLogin ? '请登录您的账号继续使用' : '填写信息注册新账号' }}</p>
          </div>

          <t-form
            ref="formRef"
            :data="formData"
            :rules="rules"
            @submit="handleSubmit"
            label-width="0"
            class="login-form"
          >
            <t-form-item v-if="!isLogin" name="nickname">
              <t-input v-model="formData.nickname" placeholder="请输入昵称（可选）" size="large" clearable>
                <template #prefix-icon><UserIcon /></template>
              </t-input>
            </t-form-item>

            <t-form-item name="username">
              <t-input v-model="formData.username" placeholder="请输入用户名" size="large" clearable>
                <template #prefix-icon><UserIcon /></template>
              </t-input>
            </t-form-item>

            <t-form-item name="password">
              <t-input v-model="formData.password" type="password" placeholder="请输入密码" size="large" clearable>
                <template #prefix-icon><LockOnIcon /></template>
              </t-input>
            </t-form-item>

            <t-form-item v-if="!isLogin" name="confirmPassword">
              <t-input v-model="formData.confirmPassword" type="password" placeholder="请再次输入密码" size="large" clearable>
                <template #prefix-icon><LockOnIcon /></template>
              </t-input>
            </t-form-item>

            <div v-if="isLogin" class="form-extras">
              <t-checkbox v-model="rememberMe">记住我</t-checkbox>
              <a href="#" class="forgot-link">忘记密码？</a>
            </div>

            <t-form-item>
              <t-button theme="primary" type="submit" block size="large" :loading="loading" class="submit-btn">
                {{ isLogin ? '登 录' : '注 册' }}
              </t-button>
            </t-form-item>
          </t-form>

          <div class="switch-row">
            <span>{{ isLogin ? '还没有账号？' : '已有账号？' }}</span>
            <a href="#" @click.prevent="toggleMode">{{ isLogin ? '立即注册' : '返回登录' }}</a>
          </div>

          <div v-if="isLogin" class="social-login">
            <t-divider>其他方式登录</t-divider>
            <div class="social-icons">
              <t-tooltip content="微信登录"><div class="social-btn wechat"><LogoWechatpayIcon size="18" /></div></t-tooltip>
              <t-tooltip content="GitHub登录"><div class="social-btn github"><LogoGithubIcon size="18" /></div></t-tooltip>
              <t-tooltip content="企业微信"><div class="social-btn wecom"><LogoWecomIcon size="18" /></div></t-tooltip>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="login-footer">
      &copy; 2024 LowCode Platform · 低代码开发平台
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import {
  CheckCircleFilledIcon, UserIcon, LockOnIcon,
  LogoWechatpayIcon, LogoGithubIcon, LogoWecomIcon
} from 'tdesign-icons-vue-next';
import { useUserStore } from '../../store';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const loading = ref(false);
const isLogin = ref(true);
const rememberMe = ref(false);

const formData = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
});

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 64, message: '用户名长度在 3 到 64 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '密码长度在 6 到 64 个字符', trigger: 'blur' },
  ],
  nickname: [
    { max: 64, message: '昵称长度不能超过 64 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: (val: string) => val === formData.password, message: '两次输入的密码不一致', trigger: 'blur' },
  ],
};

function toggleMode() {
  isLogin.value = !isLogin.value;
  formData.username = '';
  formData.password = '';
  formData.confirmPassword = '';
  formData.nickname = '';
}

async function handleSubmit({ validateResult }: any) {
  if (validateResult !== true) return;

  loading.value = true;
  try {
    if (isLogin.value) {
      const result = await userStore.loginByPassword({
        username: formData.username,
        password: formData.password,
      });
      if (result.ok) {
        MessagePlugin.success('登录成功');
        const redirect = route.query.redirect as string;
        await router.push(redirect || '/home');
      } else {
        MessagePlugin.error(result.msg || '登录失败');
      }
    } else {
      const result = await userStore.register({
        username: formData.username,
        password: formData.password,
        nickname: formData.nickname || undefined,
      });
      if (result.ok) {
        MessagePlugin.success('注册成功，已自动登录');
        const redirect = route.query.redirect as string;
        await router.push(redirect || '/home');
      } else {
        MessagePlugin.error(result.msg || '注册失败');
      }
    }
  } catch (e) {
    console.error(e);
    MessagePlugin.error(isLogin.value ? '登录失败，请重试' : '注册失败，请重试');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped lang="less">
.login-wrapper {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5a623 0%, #e8a317 40%, #d4920a 100%);
  position: relative;
  overflow: hidden;
}

/* ===== 背景装饰 ===== */
.bg-layer { position: absolute; inset: 0; pointer-events: none; }
.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: #fff;
  opacity: 0.07;
  &.c1 { width: 500px; height: 500px; top: -150px; right: -100px; }
  &.c2 { width: 350px; height: 350px; bottom: -100px; left: -80px; }
  &.c3 { width: 180px; height: 180px; top: 45%; left: 15%; opacity: 0.05; }
}
.bg-dots {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(255,255,255,0.1) 1px, transparent 1px);
  background-size: 40px 40px;
}

/* ===== 登录卡片 ===== */
.login-card {
  display: flex;
  width: 960px;
  max-width: 95vw;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 30px 80px rgba(0,0,0,0.25);
  overflow: hidden;
  z-index: 10;
  animation: cardIn 0.6s ease-out;
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(30px) scale(0.97); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

/* ===== 品牌面板 ===== */
.brand-panel {
  width: 420px;
  background: linear-gradient(160deg, #f5a623 0%, #e8a317 50%, #d4920a 100%);
  padding: 60px 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    width: 300px;
    height: 300px;
    background: rgba(255,255,255,0.06);
    border-radius: 50%;
    bottom: -100px;
    right: -60px;
  }
}
.brand-inner { text-align: center; position: relative; z-index: 2; }
.brand-logo-wrap { margin-bottom: 28px; }
.brand-logo { width: 72px; height: 72px; }
.brand-name {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: 1px;
}
.brand-tagline {
  font-size: 15px;
  opacity: 0.85;
  margin: 0 0 40px;
  letter-spacing: 1px;
}
.brand-features {
  display: inline-flex;
  flex-direction: column;
  gap: 14px;
  text-align: left;
}
.bf-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  opacity: 0.9;
}

/* ===== 表单面板 ===== */
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}
.form-inner { width: 100%; max-width: 360px; }
.form-header {
  margin-bottom: 36px;
  h2 { font-size: 26px; font-weight: 700; color: #1a1a1a; margin: 0 0 8px; letter-spacing: -0.3px; }
  p { font-size: 14px; color: #999; margin: 0; }
}

.login-form {
  :deep(.t-form-item) { margin-bottom: 20px; }
  :deep(.t-input) { height: 48px; border-radius: 10px; font-size: 14px; }
}
.form-extras {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  .forgot-link { font-size: 13px; color: var(--td-brand-color, #E8A317); text-decoration: none; }
}
.submit-btn {
  height: 48px;
  border-radius: 10px !important;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
}

.switch-row {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #999;
  a { color: var(--td-brand-color, #E8A317); text-decoration: none; font-weight: 500; margin-left: 4px; }
}

/* ===== 社交登录 ===== */
.social-login {
  margin-top: 28px;
  :deep(.t-divider) { margin: 16px 0; .t-divider__inner-text { font-size: 12px; color: #ccc; } }
}
.social-icons { display: flex; justify-content: center; gap: 16px; }
.social-btn {
  width: 44px; height: 44px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all 0.3s;
  border: 1.5px solid #eee;
  color: #888;
  &:hover { transform: translateY(-3px); box-shadow: 0 6px 16px rgba(0,0,0,0.1); }
  &.wechat:hover { background: #07c160; color: #fff; border-color: #07c160; }
  &.github:hover { background: #24292e; color: #fff; border-color: #24292e; }
  &.wecom:hover { background: #3370ff; color: #fff; border-color: #3370ff; }
}

/* ===== 底部 ===== */
.login-footer {
  color: rgba(255,255,255,0.5);
  font-size: 13px;
  margin-top: 32px;
  z-index: 10;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .login-card { flex-direction: column; max-width: 420px; }
  .brand-panel { width: 100%; padding: 40px 30px; .brand-features { display: none !important; } }
  .form-panel { padding: 32px 28px; }
}
</style>
