<template>
  <div class="student-list-page">
    <t-card :bordered="false">
      <template #title>学生信息列表</template>
      <template #actions>
        <t-button theme="primary" @click="handleCreate">
          <template #icon><plus-icon /></template>
          新增
        </t-button>
      </template>

      <!-- 搜索栏 -->
      <t-form :data="searchForm" layout="inline" @submit="handleSearch" @reset="handleReset" class="search-form">
        <t-form-item label="学号" name="studentNo">
          <t-input v-model="searchForm.studentNo" placeholder="请输入学号" clearable style="width: 200px" />
        </t-form-item>
        <t-form-item label="姓名" name="name">
          <t-input v-model="searchForm.name" placeholder="请输入姓名" clearable style="width: 200px" />
        </t-form-item>
        <t-form-item label="班级" name="className">
          <t-input v-model="searchForm.className" placeholder="请输入班级" clearable style="width: 200px" />
        </t-form-item>
        <t-form-item label="学籍状态" name="status">
          <t-input v-model="searchForm.status" placeholder="请输入学籍状态" clearable style="width: 200px" />
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
import studentApi from '@/api/lowcode/generated/student';

const router = useRouter();
const loading = ref(false);
const tableData = ref<any[]>([]);
const searchForm = reactive<Record<string, any>>({});
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });

const columns: PrimaryTableCol[] = [
  { colKey: 'studentNo', title: '学号', ellipsis: true },
  { colKey: 'name', title: '姓名', ellipsis: true },
  { colKey: 'gender', title: '性别', ellipsis: true },
  { colKey: 'className', title: '班级', ellipsis: true },
  { colKey: 'grade', title: '年级', ellipsis: true },
  { colKey: 'department', title: '院系', ellipsis: true },
  { colKey: 'phone', title: '联系电话', ellipsis: true },
  { colKey: 'email', title: '邮箱', ellipsis: true },
  { colKey: 'userId', title: '关联用户', ellipsis: true },
  { colKey: 'status', title: '学籍状态', ellipsis: true },
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
    const res: any = await studentApi.list(params);
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
  router.push(`/lowcode/gen/student/edit/new`);
}
function handleEdit(row: any) {
  router.push(`/lowcode/gen/student/edit/${row.id}`);
}
async function handleDelete(row: any) {
  await studentApi.delete(row.id);
  MessagePlugin.success('删除成功');
  loadData();
}

onMounted(loadData);
</script>

<style scoped>
.student-list-page { padding: 16px; }
.search-form { margin-bottom: 16px; }
</style>
