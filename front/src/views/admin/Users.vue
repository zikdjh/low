<template>
  <div class="admin-page">
    <div class="admin-page-header">
      <h2>用户管理</h2>
      <p>查看所有用户信息，支持冻结/解冻操作</p>
    </div>

    <div class="admin-page-body">
      <t-table
        :data="users"
        :columns="columns"
        :loading="loading"
        row-key="id"
        stripe
        hover
        bordered
        size="medium"
        table-layout="auto"
      >
        <template #status="{ row }">
          <t-tag :theme="row.status === 'active' ? 'success' : 'danger'" variant="light">
            {{ row.status === 'active' ? '正常' : '已冻结' }}
          </t-tag>
        </template>
        <template #roles="{ row }">
          <t-tag
            v-for="role in row.roles"
            :key="role"
            :theme="role === 'admin' ? 'primary' : role === 'root' ? 'warning' : 'default'"
            variant="light"
            size="small"
            style="margin-right: 4px;"
          >
            {{ role === 'root' ? '超级管理员' : role === 'admin' ? '管理员' : '用户' }}
          </t-tag>
        </template>
        <template #createdAt="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
        <template #operation="{ row }">
          <t-popconfirm
            v-if="row.status === 'active'"
            content="确定冻结该用户？冻结后用户将无法登录。"
            @confirm="handleFreeze(row)"
          >
            <t-button variant="text" theme="warning" size="small">冻结</t-button>
          </t-popconfirm>
          <t-popconfirm
            v-else
            content="确定解冻该用户？"
            @confirm="handleUnfreeze(row)"
          >
            <t-button variant="text" theme="success" size="small">解冻</t-button>
          </t-popconfirm>
        </template>
      </t-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { MessagePlugin } from 'tdesign-vue-next';
import adminApi from '../../api/admin';

interface AdminUser {
  id: number;
  username: string;
  nickname?: string;
  status: string;
  roles: string[];
  createdAt: string;
}

const users = ref<AdminUser[]>([]);
const loading = ref(false);

const columns = [
  { colKey: 'id', title: 'ID', width: 70 },
  { colKey: 'username', title: '用户名', width: 140 },
  { colKey: 'nickname', title: '昵称', width: 140 },
  { colKey: 'status', title: '状态', width: 90 },
  { colKey: 'roles', title: '角色', width: 200 },
  { colKey: 'createdAt', title: '注册时间', width: 170 },
  { colKey: 'operation', title: '操作', width: 100 },
];

function formatDate(dateStr?: string) {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  });
}

async function loadUsers() {
  loading.value = true;
  try {
    const resp: any = await adminApi.listUsers();
    if (resp?.code === 1) {
      users.value = resp.data as AdminUser[];
    } else {
      MessagePlugin.error(resp?.msg || '加载用户列表失败');
    }
  } catch (e: any) {
    const detail = e?.response?.data?.msg || e?.message || '未知错误';
    console.error('加载用户列表异常', detail, e);
    if (e?.response?.status === 401 || e?.response?.status === 403) {
      MessagePlugin.error('权限不足，请重新登录');
    } else {
      MessagePlugin.error('加载用户列表失败: ' + detail);
    }
  } finally {
    loading.value = false;
  }
}

async function handleFreeze(row: AdminUser) {
  try {
    const resp: any = await adminApi.freezeUser(row.id);
    if (resp?.code === 1) {
      MessagePlugin.success('已冻结用户');
      row.status = 'frozen';
    } else {
      MessagePlugin.error(resp?.msg || '操作失败');
    }
  } catch (e: any) {
    console.error('冻结用户异常', e);
    MessagePlugin.error('操作失败');
  }
}

async function handleUnfreeze(row: AdminUser) {
  try {
    const resp: any = await adminApi.unfreezeUser(row.id);
    if (resp?.code === 1) {
      MessagePlugin.success('已解冻用户');
      row.status = 'active';
    } else {
      MessagePlugin.error(resp?.msg || '操作失败');
    }
  } catch (e: any) {
    console.error('解冻用户异常', e);
    MessagePlugin.error('操作失败');
  }
}

onMounted(() => { loadUsers(); });
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
</style>
