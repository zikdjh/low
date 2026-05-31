<template>
  <div class="login-form">
    <div class="form-header">
      <h2 class="form-title">欢迎回来</h2>
      <p class="form-subtitle">没有账号会自动注册</p>
    </div>

    <t-form
        ref="formRef"
        :data="formData"
        :rules="formRules"
        label-width="0"
        @submit="onSubmit"
    >
      <t-form-item name="phone">
        <t-input
            v-model="formData.phone"
            size="large"
            placeholder="请输入手机号"
            clearable
        >
          <template #prefix-icon>
            <t-icon name="mobile"/>
          </template>
        </t-input>
      </t-form-item>

      <!-- 验证码登录 -->
      <t-form-item name="code">
        <t-input
            v-model="formData.code"
            size="large"
            placeholder="请输入6位验证码"
            clearable
            maxlength="6"
        >
          <template #prefix-icon>
            <t-icon name="secured"/>
          </template>
          <template #suffix>
            <t-button
                variant="text"
                size="small"
                :disabled="smsCountdown > 0"
                @click="sendSmsCode"
                class="sms-button"
            >
              {{ smsCountdown > 0 ? `${smsCountdown}s后重发` : '获取验证码' }}
            </t-button>
          </template>
        </t-input>
      </t-form-item>

      <t-form-item>
        <t-button
            theme="primary"
            size="large"
            type="submit"
            block
            :loading="loading"
            class="login-button"
        >
          立即登录
        </t-button>
      </t-form-item>
    </t-form>

    <!-- 第三方登录区域 -->
    <div class="divider">
      <span>或</span>
    </div>

    <div class="social-login">
      <t-button
          variant="outline"
          size="large"
          block
          class="social-button"
          @click="handleWechatLogin"
      >
        <t-icon name="logo-wechat" class="social-icon wechat"/>
        微信登录
      </t-button>
    </div>
  </div>
</template>


<script setup lang="ts">
import {ref, reactive, computed} from 'vue';
import {MessagePlugin} from 'tdesign-vue-next';
import type {FormInstanceFunctions, SubmitContext} from 'tdesign-vue-next';
import type {LoginForm} from "../../../api/model/user/Login.ts";
import {validateCode, validatePhone} from "../../../utils/formatVerification.ts";
import userApi from "../../../api/user";
import {useRouter} from "vue-router";

const router = useRouter();

const loading = ref(false);
const formRef = ref<FormInstanceFunctions>();
const smsCountdown = ref(0);

// 表单数据
const formData = reactive<LoginForm>({
  phone: '',
  code: '',
});

// 表单验证规则
const formRules = computed(() => ({
  phone: [
    {required: true, message: '请输入手机号', type: 'error'},
    {
      validator: (val: string) => validatePhone(val),
      message: '请输入正确的手机号格式',
      type: 'error'
    }
  ],
  code: [
    {required: true, message: '请输入验证码', type: 'error'},
    {
      validator: (val: string) => validateCode(val),
      message: '请输入6位数字验证码',
      type: 'error'
    }
  ]
}));

// 获取短信验证码
const sendSmsCode = async () => {
  if (!validatePhone(formData.phone)) {
    await MessagePlugin.error('请先输入正确格式的手机号');
    return;
  }

  try {
    const response = await userApi.getPhoneCode(formData.phone);

    if (response.code === 1) {
      await MessagePlugin.success('验证码发送成功，有效时间5分钟');
    } else {
      await MessagePlugin.error(response.msg);
    }

    // 开始倒计时
    smsCountdown.value = 60;
    const timer = setInterval(() => {
      smsCountdown.value--;
      if (smsCountdown.value <= 0) {
        clearInterval(timer);
      }
    }, 1000);

  } catch (error) {
    console.error('Send SMS error:', error);
  }
};

// 第三方登录
const handleWechatLogin = () => {
  MessagePlugin.info('微信登录功能开发中...');
};

// 提交表单
const onSubmit = async ({validateResult}: SubmitContext) => {
  if (validateResult !== true) return;

  loading.value = true;

  try {
    const response = await userApi.login(formData);
    if (response.code === 1) {
      const accessPayload = {
        token: response.data,
        expiresAt: Date.now() + 45 * 60 * 1000
      };
      sessionStorage.setItem('access', JSON.stringify(accessPayload));
      await MessagePlugin.success('登录成功');

      // 跳转到首页
      await router.push('/home')
    } else if (response.code === 0) {
      await MessagePlugin.error(response.msg);
    } else {
      await MessagePlugin.error(response.msg);
    }
  } catch (error) {
    await MessagePlugin.error('登录失败，请检查登录信息');
    console.error('Login error:', error);
  } finally {
    loading.value = false;
  }
};
</script>


<style scoped>
.login-form {
  width: 100%;
}

.form-header {
  text-align: center;
  margin-bottom: 2rem;
}

.form-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--td-text-color-primary);
  margin: 0 0 0.5rem 0;
}

.form-subtitle {
  color: var(--td-text-color-placeholder);
  font-size: 0.95rem;
  margin: 0;
}

.sms-button {
  font-size: 0.85rem;
  padding: 0 8px;
  min-width: 80px;
  color: var(--td-brand-color);
}

.login-button {
  height: 48px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 8px;
  background: var(--td-brand-color);
  border: none;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-1px);
  background: var(--td-brand-color-7);
  box-shadow: 0 10px 25px var(--td-brand-color-1);
}

.divider {
  position: relative;
  text-align: center;
  margin: 1.5rem 0;
  color: var(--td-text-color-placeholder);
  font-size: 0.9rem;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--td-component-stroke);
  z-index: 1;
}

.divider span {
  background: var(--td-bg-color-container);
  padding: 0 1rem;
  position: relative;
  z-index: 2;
}

.social-login {
  margin-top: 1rem;
}

.social-button {
  height: 48px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
  border-color: var(--td-brand-color);
  color: var(--td-brand-color);
}

.social-button:hover {
  transform: translateY(-1px);
  background: var(--td-brand-color-1);
  border-color: var(--td-brand-color-7);
  box-shadow: 0 5px 15px var(--td-brand-color-1);
}

.social-icon {
  margin-right: 0.5rem;
}

.wechat {
  color: #1aad19;
}

:deep(.t-form-item) {
  margin-bottom: 1.25rem;
}

:deep(.t-input) {
  border-radius: 8px;
}

:deep(.t-input__inner) {
  height: 48px;
  font-size: 1rem;
}

:deep(.t-radio-group) {
  width: 100%;
}

:deep(.t-radio-button) {
  flex: 1;
}

:deep(.t-radio-button.t-is-checked) {
  background: var(--td-brand-color);
  border-color: var(--td-brand-color);
}

:deep(.t-radio-button:hover) {
  border-color: var(--td-brand-color);
}
</style>