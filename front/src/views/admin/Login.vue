<template>
  <div class="admin-login-wrapper">
    <!-- 背景装饰 -->
    <div class="admin-bg-layer">
      <div class="admin-bg-circle c1"></div>
      <div class="admin-bg-circle c2"></div>
      <div class="admin-bg-circle c3"></div>
      <div class="admin-bg-dots"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="admin-login-card">
      <div class="admin-card-brand">
        <svg viewBox="0 0 60 60" fill="none" class="admin-brand-logo">
          <rect width="60" height="60" rx="14" fill="url(#adminBrandLogoGrad)" />
          <path d="M18 21l12-6 12 6-12 6-12-6z" fill="#fff" opacity="0.9" />
          <path d="M18 30l12 6 12-6M18 39l12 6 12-6" stroke="#fff" stroke-width="2.5" stroke-linecap="round" opacity="0.75" />
          <defs>
            <linearGradient id="adminBrandLogoGrad" x1="0" y1="0" x2="60" y2="60">
              <stop offset="0%" stop-color="#f5a623" />
              <stop offset="100%" stop-color="#d4920a" />
            </linearGradient>
          </defs>
        </svg>
        <h1>管理后台</h1>
        <p>LowCode 低代码开发平台</p>
      </div>

      <div class="admin-card-body">
        <t-form
          ref="formRef"
          :data="formData"
          :rules="rules"
          @submit="handleSubmit"
          label-width="0"
          class="admin-login-form"
        >
          <t-form-item name="username">
            <t-input
              v-model="formData.username"
              placeholder="请输入管理员账号"
              size="large"
              clearable
            >
              <template #prefix-icon><UserIcon /></template>
            </t-input>
          </t-form-item>

          <t-form-item name="password">
            <t-input
              v-model="formData.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              clearable
            >
              <template #prefix-icon><LockOnIcon /></template>
            </t-input>
          </t-form-item>

          <t-form-item>
            <t-button
              type="submit"
              theme="primary"
              block
              size="large"
              :loading="loading"
              class="admin-login-btn"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </t-button>
          </t-form-item>
        </t-form>
      </div>
    </div>

    <div class="admin-login-bottom">
      &copy; 2024 LowCode Platform · 管理后台
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import { UserIcon, LockOnIcon } from 'tdesign-icons-vue-next';
import { useUserStore } from '../../store';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const formRef = ref();
const loading = ref(false);

const formData = reactive({
  username: '',
  password: '',
});

const rules = {
  username: [{ required: true, message: '请输入管理员账号' }],
  password: [{ required: true, message: '请输入密码' }],
};

async function handleSubmit() {
  const valid = await formRef.value?.validate();
  if (valid !== true) return;

  loading.value = true;
  try {
    const result = await userStore.loginByPassword({
      username: formData.username,
      password: formData.password,
    });

    if (!result.ok) {
      MessagePlugin.error(result.msg || '登录失败');
      return;
    }

    // 检查是否有 admin/root 角色
    const roles = userStore.roles;
    if (!roles.includes('admin') && !roles.includes('root')) {
      MessagePlugin.error('该账号无管理员权限');
      await userStore.logout(false);
      return;
    }

    MessagePlugin.success('登录成功');
    const redirect = (route.query.redirect as string) || '/admin/users';
    router.push(redirect);
  } catch {
    MessagePlugin.error('登录失败，请检查网络');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped lang="less">
/* ===== 页面整体 ===== */
.admin-login-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5a623 0%, #e8a317 40%, #d4920a 100%);
  position: relative;
  overflow: hidden;
}

/* ===== 背景装饰（与用户端一致） ===== */
.admin-bg-layer { position: absolute; inset: 0; pointer-events: none; }
.admin-bg-circle {
  position: absolute;
  border-radius: 50%;
  background: #fff;
  opacity: 0.07;
  &.c1 { width: 500px; height: 500px; top: -150px; right: -100px; }
  &.c2 { width: 350px; height: 350px; bottom: -100px; left: -80px; }
  &.c3 { width: 180px; height: 180px; top: 45%; left: 15%; opacity: 0.05; }
}
.admin-bg-dots {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(255,255,255,0.1) 1px, transparent 1px);
  background-size: 40px 40px;
}

/* ===== 登录卡片 ===== */
.admin-login-card {
  position: relative;
  z-index: 10;
  width: 420px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.25);
  overflow: hidden;
  animation: cardIn 0.6s ease-out;
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(30px) scale(0.97); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

/* 品牌头部（白色背景，与卡片 body 一致） */
.admin-card-brand {
  background: #fff;
  padding: 36px 32px 20px;
  text-align: center;
  color: #1a1a1a;
  border-bottom: 1px solid #f0f0f0;
  .admin-brand-logo { width: 56px; height: 56px; margin-bottom: 12px; }
  h1 { font-size: 24px; font-weight: 700; margin: 0 0 4px; letter-spacing: 1px; }
  p { font-size: 13px; color: #999; margin: 0; }
}

/* 表单区域 */
.admin-card-body {
  padding: 32px 36px 28px;
}
.admin-login-form {
  :deep(.t-form__item) { margin-bottom: 20px; }
  :deep(.t-input) { height: 46px; border-radius: 10px; font-size: 14px; }
}
.admin-login-btn {
  margin-top: 4px;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  border-radius: 10px !important;
}
/* 底部 */
.admin-login-bottom {
  color: rgba(255,255,255,0.5);
  font-size: 13px;
  margin-top: 24px;
  z-index: 10;
}
</style>
