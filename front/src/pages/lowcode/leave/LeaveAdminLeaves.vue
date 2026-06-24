<template>
  <div class="admin-leaves-page">
    <div class="page-header">
      <h2>假条管理</h2>
      <p>查看和删除所有请假记录</p>
    </div>

    <div class="filters">
      <input
        v-model="searchKeyword"
        type="text"
        class="filter-input"
        placeholder="搜索学生姓名..."
        @keyup.enter="loadLeaves"
      />
      <select v-model="filterStatus" class="filter-select" @change="loadLeaves">
        <option value="">全部状态</option>
        <option value="pending_counselor">待辅导员审核</option>
        <option value="pending_dean">待系主任审核</option>
        <option value="approved">审核通过</option>
        <option value="rejected">已驳回</option>
        <option value="cancelled">已撤回</option>
      </select>
    </div>

    <div class="table-wrapper">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>学生姓名</th>
            <th>请假类型</th>
            <th>请假原因</th>
            <th>开始日期</th>
            <th>结束日期</th>
            <th>天数</th>
            <th>审核状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="leave in leaves" :key="leave.id">
            <td>{{ leave.id }}</td>
            <td>{{ leave.student_name || '-' }}</td>
            <td>
              <span class="type-tag">{{ leaveTypeLabel(leave.leave_type) }}</span>
            </td>
            <td class="reason-cell">{{ leave.reason }}</td>
            <td>{{ leave.start_date }}</td>
            <td>{{ leave.end_date }}</td>
            <td>{{ leave.leave_days }}天</td>
            <td>
              <span class="status-tag" :class="statusClass(leave.status)">
                {{ statusLabel(leave.status) }}
              </span>
            </td>
            <td>
              <button class="btn btn-delete" @click="handleDelete(leave)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="leaves.length === 0 && !loading" class="empty">暂无假条数据</div>
    </div>

    <t-loading v-if="loading" size="large" text="加载中..." class="loading-overlay" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import request from '../../../api/index';
import { MessagePlugin } from 'tdesign-vue-next';

interface LeaveRecord {
  id: number;
  student_id: number;
  student_name: string;
  leave_type: string;
  reason: string;
  start_date: string;
  end_date: string;
  leave_days: number;
  status: string;
}

const leaves = ref<LeaveRecord[]>([]);
const loading = ref(false);
const searchKeyword = ref('');
const filterStatus = ref('');

const leaveTypeMap: Record<string, string> = {
  sick: '病假',
  personal: '事假',
  public: '公假',
  other: '其他',
};

const statusMap: Record<string, string> = {
  pending_counselor: '待辅导员审核',
  pending_dean: '待系主任审核',
  approved: '审核通过',
  rejected: '已驳回',
  cancelled: '已撤回',
};

function leaveTypeLabel(type: string): string {
  return leaveTypeMap[type] || type || '-';
}

function statusLabel(status: string): string {
  return statusMap[status] || status || '-';
}

function statusClass(status: string): string {
  if (status === 'approved') return 'approved';
  if (status?.includes('pending')) return 'pending';
  if (status === 'rejected') return 'rejected';
  if (status === 'cancelled') return 'cancelled';
  return '';
}

async function loadLeaves() {
  loading.value = true;
  try {
    const params: any = { page: 1, pageSize: 200 };
    if (filterStatus.value) params.status = filterStatus.value;
    const res = await request.get('/lowcode/leave/records', { params });
    if (res.data.code === 1 || res.data.code === 0) {
      const data = res.data.data;
      leaves.value = data?.content || data?.records || [];
    } else {
      leaves.value = Array.isArray(res.data) ? res.data : [];
    }
  } catch (e: any) {
    MessagePlugin.error('加载假条列表失败');
  } finally {
    loading.value = false;
  }
}

async function handleDelete(leave: LeaveRecord) {
  if (!confirm(`确定要删除该假条吗？（学生：${leave.student_name}，ID：${leave.id}）`)) return;
  try {
    await request.delete(`/lowcode/leave/admin/leaves/${leave.id}`);
    MessagePlugin.success('假条已删除');
    loadLeaves();
  } catch (e: any) {
    MessagePlugin.error('删除失败');
  }
}

onMounted(() => {
  loadLeaves();
});
</script>

<style scoped>
.admin-leaves-page {
  padding: 24px;
}
.page-header {
  margin-bottom: 20px;
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
.filters {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.filter-input {
  padding: 8px 14px;
  border: 1.5px solid #e5e5e5;
  border-radius: 8px;
  font-size: 13px;
  outline: none;
  width: 220px;
}
.filter-input:focus { border-color: #e8a317; }
.filter-select {
  padding: 8px 12px;
  border: 1.5px solid #e5e5e5;
  border-radius: 8px;
  font-size: 13px;
  outline: none;
  background: #fff;
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
.data-table tr:hover td { background: #fafbfd; }
.reason-cell { max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.type-tag { display: inline-block; padding: 2px 8px; border-radius: 12px; font-size: 12px; background: #e8f4fd; color: #2563eb; }
.status-tag { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.status-tag.pending { background: #fef3c7; color: #b45309; }
.status-tag.approved { background: #dcfce7; color: #15803d; }
.status-tag.rejected { background: #fee2e2; color: #b91c1c; }
.status-tag.cancelled { background: #f0f0f0; color: #666; }
.btn {
  padding: 5px 14px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-delete { background: #fee2e2; color: #b91c1c; }
.btn-delete:hover { background: #fecaca; }
.empty { padding: 60px; text-align: center; color: #999; font-size: 14px; }
.loading-overlay { position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); }
</style>
