<template>
  <div class="admin-users-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <p>管理系统用户，可冻结或解冻账号</p>
    </div>
    
    <div class="table-wrapper">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>昵称</th>
            <th>角色</th>
            <th>状态</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.nickname || '-' }}</td>
            <td>
              <span
                v-for="role in user.roles"
                :key="role.code"
                class="role-tag"
                :class="'role-' + role.code"
              >{{ role.name }}</span>
            </td>
            <td>
              <span class="status-tag" :class="user.status">
                {{ user.status === 'active' ? '正常' : '已冻结' }}
              </span>
            </td>
            <td>{{ formatTime(user.createdAt) }}</td>
            <td>
              <div class="action-btns">
                <button
                  v-if="user.status === 'active'"
                  class="btn btn-freeze"
                  @click="handleFreeze(user)"
                >冻结</button>
                <button
                  v-else
                  class="btn btn-unfreeze"
                  @click="handleUnfreeze(user)"
                >解冻</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="users.length === 0 && !loading" class="empty">暂无用户数据</div>
    </div>
    
    <t-loading v-if="loading" size="large" text="加载中..." class="loading-overlay" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import request from '../../../api/index';
import { MessagePlugin } from 'tdesign-vue-next';

interface UserInfo {
  id: number;
  username: string;
  nickname: string;
  status: string;
  createdAt: string;
  roles: { code: string; name: string }[];
}

const users = ref<UserInfo[]>([]);
const loading = ref(false);

async function loadUsers() {
  loading.value = true;
  try {
    const res = await request.get('/lowcode/leave/admin/users');
    if (res.data.code === 1 || res.data.code === 0) {
      users.value = res.data.data || [];
    } else {
      users.value = Array.isArray(res.data) ? res.data : [];
    }
  } catch (e: any) {
    MessagePlugin.error('加载用户列表失败');
  } finally {
    loading.value = false;
  }
}

async function handleFreeze(user: UserInfo) {
  if (!confirm(`确定要冻结用户 "${user.username}" 吗？`)) return;
  try {
    await request.put(`/lowcode/leave/admin/users/${user.id}/freeze`);
    MessagePlugin.success(`用户 "${user.username}" 已冻结`);
    loadUsers();
  } catch (e: any) {
    MessagePlugin.error('操作失败');
  }
}

async function handleUnfreeze(user: UserInfo) {
  if (!confirm(`确定要解冻用户 "${user.username}" 吗？`)) return;
  try {
    await request.put(`/lowcode/leave/admin/users/${user.id}/unfreeze`);
    MessagePlugin.success(`用户 "${user.username}" 已解冻`);
    loadUsers();
  } catch (e: any) {
    MessagePlugin.error('操作失败');
  }
}

function formatTime(time: string): string {
  if (!time) return '-';
  return new Date(time).toLocaleString('zh-CN');
}

onMounted(() => {
  loadUsers();
});
</script>

<style scoped>
.admin-users-page {
  padding: 24px;
}
.page-header {
  margin-bottom: 24px;
}
.page-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 4px;
}
.page-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}
.table-wrapper {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  overflow: hidden;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table th {
  background: #f8f9fb;
  font-size: 13px;
  font-weight: 600;
  color: #666;
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #eee;
}
.data-table td {
  padding: 12px 16px;
  font-size: 14px;
  color: #333;
  border-bottom: 1px solid #f5f5f5;
}
.data-table tr:hover td {
  background: #fafbfd;
}
.role-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  margin-right: 4px;
}
.role-tag.role-student { background: #e0f2fe; color: #0369a1; }
.role-tag.role-counselor { background: #fef3c7; color: #b45309; }
.role-tag.role-dept_head { background: #ede9fe; color: #6d28d9; }
.role-tag.role-admin { background: #fce7f3; color: #be185d; }
.role-tag.role-user { background: #f0fdf4; color: #15803d; }
.status-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}
.status-tag.active { background: #dcfce7; color: #15803d; }
.status-tag.frozen { background: #fee2e2; color: #b91c1c; }
.action-btns {
  display: flex;
  gap: 8px;
}
.btn {
  padding: 5px 14px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-freeze { background: #fee2e2; color: #b91c1c; }
.btn-freeze:hover { background: #fecaca; }
.btn-unfreeze { background: #dcfce7; color: #15803d; }
.btn-unfreeze:hover { background: #bbf7d0; }
.empty {
  padding: 60px;
  text-align: center;
  color: #999;
  font-size: 14px;
}
.loading-overlay {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
</style>
