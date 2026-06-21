<template>
  <div class="admin-page">
    <div class="admin-page-header">
      <h2>个人信息</h2>
      <p>修改管理员个人信息</p>
    </div>

    <div class="admin-page-body">
      <div class="profile-card">
        <div class="profile-avatar-area">
          <t-avatar size="80px">
            <template #icon><UserIcon size="40px" /></template>
          </t-avatar>
        </div>

        <t-form
          ref="formRef"
          :data="formData"
          :rules="rules"
          label-width="80px"
          class="profile-form"
        >
          <t-form-item label="用户ID">
            <t-input :value="profile.id" disabled />
          </t-form-item>
          <t-form-item label="用户名">
            <t-input :value="profile.username" disabled />
          </t-form-item>
          <t-form-item label="角色">
            <t-tag
              v-for="role in profile.roles"
              :key="role"
              :theme="role === 'admin' ? 'primary' : 'warning'"
              variant="light"
              size="small"
              style="margin-right: 4px;"
            >
              {{ role === 'root' ? '超级管理员' : '管理员' }}
            </t-tag>
          </t-form-item>
          <t-form-item label="昵称" name="nickname">
            <t-input v-model="formData.nickname" placeholder="请输入昵称" />
          </t-form-item>
          <t-form-item label="新密码" name="password">
            <t-input
              v-model="formData.password"
              type="password"
              placeholder="留空则不修改密码"
            />
          </t-form-item>
          <t-form-item label="确认密码" name="confirmPassword">
            <t-input
              v-model="formData.confirmPassword"
              type="password"
              placeholder="若修改密码请再次输入"
            />
          </t-form-item>
          <t-form-item>
            <t-button theme="primary" :loading="saving" @click="handleSave">
              {{ saving ? '保存中...' : '保存修改' }}
            </t-button>
          </t-form-item>
        </t-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { MessagePlugin } from 'tdesign-vue-next';
import { UserIcon } from 'tdesign-icons-vue-next';
import adminApi from '../../api/admin';
import { useUserStore } from '../../store';

const userStore = useUserStore();
const formRef = ref();
const saving = ref(false);

const profile = reactive({
  id: 0,
  username: '',
  roles: [] as string[],
});

const formData = reactive({
  nickname: '',
  password: '',
  confirmPassword: '',
});

const rules = {
  confirmPassword: [
    {
      validator: () => {
        if (formData.password && formData.password !== formData.confirmPassword) {
          return { result: false, message: '两次输入的密码不一致' };
        }
        return { result: true };
      },
      trigger: 'blur',
    },
  ],
};

async function loadProfile() {
  try {
    const resp: any = await adminApi.getProfile();
    if (resp?.code === 1) {
      const data = resp.data as any;
      profile.id = data.id;
      profile.username = data.username;
      profile.roles = data.roles || [];
      formData.nickname = data.nickname || '';
    } else {
      MessagePlugin.error(resp?.msg || '加载个人信息失败');
    }
  } catch (e: any) {
    console.error('加载个人信息异常', e);
    MessagePlugin.error('加载个人信息失败');
  }
}

async function handleSave() {
  const valid = await formRef.value?.validate();
  if (valid !== true) return;

  if (formData.password && formData.password !== formData.confirmPassword) {
    MessagePlugin.error('两次输入的密码不一致');
    return;
  }

  saving.value = true;
  try {
    const payload: Record<string, string> = { nickname: formData.nickname };
    if (formData.password) {
      payload.password = formData.password;
    }
    const resp: any = await adminApi.updateProfile(payload);
    if (resp?.code === 1) {
      MessagePlugin.success('信息更新成功');
      formData.password = '';
      formData.confirmPassword = '';
      if (userStore.userInfo) {
        userStore.userInfo.nickname = formData.nickname;
      }
    } else {
      MessagePlugin.error(resp?.msg || '更新失败');
    }
  } catch (e: any) {
    console.error('更新个人信息异常', e);
    MessagePlugin.error('更新失败');
  } finally {
    saving.value = false;
  }
}

onMounted(() => { loadProfile(); });
</script>

<style scoped lang="less">
.admin-page {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.admin-page-header {
  margin-bottom: 24px;
  h2 { font-size: 18px; font-weight: 600; color: #1a1a1a; margin: 0 0 4px; }
  p { font-size: 13px; color: #999; margin: 0; }
}
.profile-card { max-width: 520px; }
.profile-avatar-area {
  margin-bottom: 24px;
}
.profile-form {
  :deep(.t-form__item) { margin-bottom: 20px; }
}
</style>
