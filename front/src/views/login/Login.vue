<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo-wrapper">
            <CodeIcon size="48" />
          </div>
          <h1 class="brand-title">低代码开发平台</h1>
          <p class="brand-subtitle">Low Code Development Platform</p>
          <div class="features">
            <div class="feature-item">
              <CheckCircleFilledIcon size="16" />
              <span>可视化页面设计</span>
            </div>
            <div class="feature-item">
              <CheckCircleFilledIcon size="16" />
              <span>动态数据管理</span>
            </div>
            <div class="feature-item">
              <CheckCircleFilledIcon size="16" />
              <span>快速应用构建</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单区域 -->
      <div class="form-section">
        <div class="form-header">
          <h2>{{ isLogin ? '欢迎回来' : '创建账户' }}</h2>
          <p>{{ isLogin ? '请登录您的账号' : '填写以下信息注册新账号' }}</p>
        </div>

        <t-form
          ref="formRef"
          :data="formData"
          :rules="rules"
          @submit="handleSubmit"
          label-width="0"
          class="login-form"
        >
          <!-- 注册时显示用户名 -->
          <t-form-item v-if="!isLogin" name="username">
            <t-input
              v-model="formData.username"
              placeholder="请输入用户名"
              size="large"
              clearable
            >
              <template #prefix-icon><UserIcon /></template>
            </t-input>
          </t-form-item>

          <t-form-item name="account">
            <t-input
              v-model="formData.account"
              :placeholder="isLogin ? '请输入账号/邮箱/手机号' : '请输入邮箱或手机号'"
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

        <!-- 注册时显示确认密码 -->
        <t-form-item v-if="!isLogin" name="confirmPassword">
          <t-input
            v-model="formData.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
            clearable
          >
            <template #prefix-icon><LockOnIcon /></template>
          </t-input>
        </t-form-item>

        <!-- 登录时的额外选项 -->
        <div v-if="isLogin" class="form-options">
          <t-checkbox v-model="rememberMe">记住我</t-checkbox>
          <a href="#" class="forgot-link">忘记密码？</a>
        </div>

        <t-form-item>
          <t-button
            theme="primary"
            type="submit"
            block
            size="large"
            :loading="loading"
            class="submit-btn"
          >
            {{ isLogin ? '登 录' : '注 册' }}
          </t-button>
        </t-form-item>
      </t-form>

      <!-- 切换登录/注册 -->
      <div class="switch-mode">
        <span>{{ isLogin ? '还没有账号？' : '已有账号？' }}</span>
        <a href="#" @click.prevent="toggleMode">{{ isLogin ? '立即注册' : '返回登录' }}</a>
      </div>

      <!-- 第三方登录 -->
      <div v-if="isLogin" class="third-party-login">
        <t-divider>其他登录方式</t-divider>
        <div class="social-icons">
          <t-tooltip content="微信登录">
            <div class="social-icon wechat">
              <LogoWechatpayIcon size="20" />
            </div>
          </t-tooltip>
          <t-tooltip content="GitHub登录">
            <div class="social-icon github">
              <LogoGithubIcon size="20" />
            </div>
          </t-tooltip>
          <t-tooltip content="企业微信">
            <div class="social-icon work">
              <LogoWecomIcon size="20" />
            </div>
          </t-tooltip>
        </div>
      </div>
    </div>
  </div>

    <!-- 底部信息 -->
    <div class="footer-info">
      <p>&copy; 2024 低代码开发平台 · 让开发更简单</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import {
  CodeIcon,
  CheckCircleFilledIcon,
  UserIcon,
  LockOnIcon,
  LogoWechatpayIcon,
  LogoGithubIcon,
  LogoWecomIcon
} from 'tdesign-icons-vue-next';

const router = useRouter();
const route = useRoute();

const loading = ref(false);
const isLogin = ref(true);
const rememberMe = ref(false);

const formData = reactive({
  username: '',
  account: '',
  password: '',
  confirmPassword: '',
});

// 表单验证规则
const rules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 50, message: '账号长度在 3 到 50 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度在 6 到 30 个字符', trigger: 'blur' },
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (val: string) => val === formData.password,
      message: '两次输入的密码不一致',
      trigger: 'blur',
    },
  ],
};

function toggleMode() {
  isLogin.value = !isLogin.value;
  // 清空表单
  formData.username = '';
  formData.account = '';
  formData.password = '';
  formData.confirmPassword = '';
}

async function handleSubmit({ validateResult }: any) {
  if (validateResult === true) {
    loading.value = true;

    try {
      // 模拟登录/注册请求
      await new Promise(resolve => setTimeout(resolve, 1500));

      if (isLogin.value) {
        // 登录逻辑
        const token = 'mock_token_' + Date.now();
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify({
          username: formData.account || 'admin',
          role: 'admin',
          avatar: '',
        }));
        // 同时设置 API 请求所需的 sessionStorage
        const accessPayload = { token, expiresAt: Date.now() + 45 * 60 * 1000 };
        sessionStorage.setItem('access', JSON.stringify(accessPayload));

        MessagePlugin.success('登录成功！');

        // 跳转到目标页面或首页
        const redirect = route.query.redirect as string;
        setTimeout(() => {
          router.push(redirect || '/home');
        }, 500);
      } else {
        // 注册逻辑
        MessagePlugin.success('注册成功！请登录');
        isLogin.value = true;
        formData.password = '';
        formData.confirmPassword = '';
      }
    } catch (error) {
      MessagePlugin.error(isLogin.value ? '登录失败，请重试' : '注册失败，请重试');
    } finally {
      loading.value = false;
    }
  }
}
</script>

<style scoped lang="less">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;

  .circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.1;
    background: #fff;

    &.circle-1 {
      width: 400px;
      height: 400px;
      top: -100px;
      right: -100px;
      animation: float 6s ease-in-out infinite;
    }

    &.circle-2 {
      width: 300px;
      height: 300px;
      bottom: -80px;
      left: -80px;
      animation: float 8s ease-in-out infinite reverse;
    }

    &.circle-3 {
      width: 200px;
      height: 200px;
      top: 50%;
      left: 10%;
      animation: float 7s ease-in-out infinite 1s;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
  }
}

.login-card {
  display: flex;
  width: 900px;
  max-width: 95vw;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  z-index: 10;
  animation: slideUp 0.5s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.brand-section {
  width: 400px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;

  .brand-content {
    text-align: center;

    .logo-wrapper {
      width: 96px;
      height: 96px;
      background: rgba(255, 255, 255, 0.15);
      border-radius: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 28px;
      backdrop-filter: blur(10px);
    }

    .brand-title {
      font-size: 28px;
      font-weight: 700;
      margin: 0 0 12px 0;
      letter-spacing: 2px;
    }

    .brand-subtitle {
      font-size: 14px;
      opacity: 0.85;
      margin: 0 0 36px 0;
      letter-spacing: 1px;
    }

    .features {
      text-align: left;
      display: inline-block;

      .feature-item {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 18px;
        font-size: 14px;
        opacity: 0.9;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}

.form-section {
  flex: 1;
  padding: 50px 45px;
  display: flex;
  flex-direction: column;

  .form-header {
    margin-bottom: 32px;

    h2 {
      font-size: 26px;
      font-weight: 600;
      color: #1f2329;
      margin: 0 0 8px 0;
    }

    p {
      font-size: 14px;
      color: #8f959e;
      margin: 0;
    }
  }

  .login-form {
    flex: 1;

    :deep(.t-form-item) {
      margin-bottom: 22px;
    }

    :deep(.t-input) {
      height: 46px;
      border-radius: 8px;
      font-size: 14px;
    }
  }

  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 22px;

    .forgot-link {
      font-size: 13px;
      color: #1677ff;
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .submit-btn {
    height: 46px;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 500;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;

    &:hover:not(:disabled) {
      background: linear-gradient(135deg, #5568d3 0%, #654191 100%);
    }
  }

  .switch-mode {
    text-align: center;
    margin-top: 20px;
    font-size: 14px;
    color: #8f959e;

    a {
      color: #1677ff;
      text-decoration: none;
      font-weight: 500;
      margin-left: 4px;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .third-party-login {
    margin-top: 24px;

    :deep(.t-divider) {
      margin: 16px 0;

      .t-divider__inner-text {
        font-size: 12px;
        color: #bbbfc4;
      }
    }

    .social-icons {
      display: flex;
      justify-content: center;
      gap: 20px;

      .social-icon {
        width: 42px;
        height: 42px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.3s;
        border: 1px solid #e8e8e8;

        &:hover {
          transform: translateY(-3px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        &.wechat {
          color: #07c160;

          &:hover {
            background: #07c160;
            color: #fff;
            border-color: #07c160;
          }
        }

        &.github {
          color: #333;

          &:hover {
            background: #333;
            color: #fff;
            border-color: #333;
          }
        }

        &.work {
          color: #3370ff;

          &:hover {
            background: #3370ff;
            color: #fff;
            border-color: #3370ff;
          }
        }
      }
    }
  }
}

.footer-info {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
  color: rgba(255, 255, 255, 0.75);
  font-size: 13px;
  z-index: 5;
}

@media (max-width: 768px) {
  .login-card {
    flex-direction: column;
    width: 95vw;
    max-width: 420px;
  }

  .brand-section {
    width: 100%;
    padding: 40px 30px;

    .features {
      display: none !important;
    }
  }

  .form-section {
    padding: 35px 30px;
  }
}
</style>
