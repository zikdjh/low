<template>
  <div class="leaveApplication-list-page">
    <t-card :bordered="false">
      <template #title>请假申请列表</template>
      <template #actions>
        <t-button theme="primary" @click="handleCreate">
          <template #icon><plus-icon /></template>
          新增
        </t-button>
      </template>

      <!-- 搜索栏 -->
      <t-form :data="searchForm" layout="inline" @submit="handleSearch" @reset="handleReset" class="search-form">
        <t-form-item label="学生" name="studentId">
          <t-input v-model="searchForm.studentId" placeholder="请输入学生" clearable style="width: 200px" />
        </t-form-item>
        <t-form-item label="学生姓名" name="studentName">
          <t-input v-model="searchForm.studentName" placeholder="请输入学生姓名" clearable style="width: 200px" />
        </t-form-item>
        <t-form-item label="请假类型" name="leaveType">
          <t-input v-model="searchForm.leaveType" placeholder="请输入请假类型" clearable style="width: 200px" />
        </t-form-item>
        <t-form-item label="审批状态" name="status">
          <t-input v-model="searchForm.status" placeholder="请输入审批状态" clearable style="width: 200px" />
        </t-form-item>
        <t-form-item>
          <t-button theme="primary" type="submit">查询</t-button>
          <t-button theme="default" type="reset" style="margin-left: 8px">重置</t-button>
        </t-form-item>
      </t-form>

      <t-table
        :data="tableData"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        bordered
        stripe
        @page-change="onPageChange"
      >
        <template #operation="{ row }">
          <t-link theme="primary" @click="handleEdit(row)">编辑</t-link>
          <t-popconfirm content="确认删除?" @confirm="handleDelete(row)">
            <t-link theme="danger" style="margin-left: 12px">删除</t-link>
          </t-popconfirm>
        </template>
      </t-table>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { PlusIcon } from 'tdesign-icons-vue-next';
import leaveApplicationApi from '@/api/lowcode/generated/leaveApplication';

const router = useRouter();
const loading = ref(false);
const tableData = ref<any[]>([]);
const searchForm = reactive<Record<string, any>>({});
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });

const columns: PrimaryTableCol[] = [
  { colKey: 'studentId', title: '学生', ellipsis: true },
  { colKey: 'studentName', title: '学生姓名', ellipsis: true },
  { colKey: 'leaveType', title: '请假类型', ellipsis: true },
  { colKey: 'reason', title: '请假原因', ellipsis: true },
  { colKey: 'startDate', title: '开始日期', ellipsis: true },
  { colKey: 'endDate', title: '结束日期', ellipsis: true },
  { colKey: 'leaveDays', title: '请假天数', ellipsis: true },
  { colKey: 'status', title: '审批状态', ellipsis: true },
  { colKey: 'operation', title: '操作', width: 160, fixed: 'right' },
];

async function loadData() {
  loading.value = true;
  try {
    const params: any = { pageNum: pagination.current, pageSize: pagination.pageSize };
    Object.keys(searchForm).forEach(k => {
      if (searchForm[k] !== undefined && searchForm[k] !== '' && searchForm[k] !== null) {
        params[k] = searchForm[k];
      }
    });
    const res: any = await leaveApplicationApi.list(params);
    tableData.value = res.data?.list || [];
    pagination.total = res.data?.total || 0;
  } catch (e: any) {
    MessagePlugin.error(e?.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.current = 1;
  loadData();
}
function handleReset() {
  Object.keys(searchForm).forEach(k => delete searchForm[k]);
  pagination.current = 1;
  loadData();
}
function onPageChange(p: { current: number; pageSize: number }) {
  pagination.current = p.current;
  pagination.pageSize = p.pageSize;
  loadData();
}
function handleCreate() {
  router.push(`/lowcode/gen/leave_application/edit/new`);
}
function handleEdit(row: any) {
  router.push(`/lowcode/gen/leave_application/edit/${row.id}`);
}
async function handleDelete(row: any) {
  await leaveApplicationApi.delete(row.id);
  MessagePlugin.success('删除成功');
  loadData();
}

onMounted(loadData);
</script>

<style scoped>
.leaveApplication-list-page { padding: 16px; }
.search-form { margin-bottom: 16px; }
</style>
