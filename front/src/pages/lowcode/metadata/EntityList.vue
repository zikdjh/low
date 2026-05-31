<template>
  <div class="entity-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>实体管理</h2>
      <t-button theme="primary" @click="handleCreate">
        <template #icon><t-icon name="add" /></template>
        新建实体
      </t-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <t-input
        v-model="keyword"
        placeholder="搜索实体名称或编码..."
        clearable
        @enter="handleSearch"
        @clear="handleSearch"
      >
        <template #suffix-icon><t-icon name="search" /></template>
      </t-input>
    </div>

    <!-- 实体列表 -->
    <t-table
      :data="tableData"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      row-key="id"
      stripe
      @page-change="onPageChange"
    >
      <template #status="{ row }">
        <t-tag v-if="row.status === 'draft'" theme="default" variant="light">草稿</t-tag>
        <t-tag v-else-if="row.status === 'published'" theme="success" variant="light">已发布</t-tag>
        <t-tag v-else-if="row.status === 'archived'" theme="warning" variant="light">已归档</t-tag>
      </template>
      <template #tableName="{ row }">
        <code>{{ row.tableName }}</code>
      </template>
      <template #operation="{ row }">
        <t-space>
          <t-link theme="primary" @click="handleEdit(row)">编辑</t-link>
          <t-link
            v-if="row.status === 'draft'"
            theme="success"
            @click="handlePublish(row)"
          >发布</t-link>
          <t-link
            v-if="row.status === 'published'"
            theme="warning"
            @click="handleArchive(row)"
          >归档</t-link>
          <t-popconfirm
            v-if="row.status === 'draft'"
            content="确认删除该实体？"
            @confirm="handleDelete(row)"
          >
            <t-link theme="danger">删除</t-link>
          </t-popconfirm>
        </t-space>
      </template>
    </t-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import type { PrimaryTableCol } from 'tdesign-vue-next';
import entityMetaApi from '../../../api/lowcode/entityMeta';

const router = useRouter();

const loading = ref(false);
const keyword = ref('');
const tableData = ref<any[]>([]);

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
});

const columns: PrimaryTableCol[] = [
  { colKey: 'id', title: 'ID', width: 80 },
  { colKey: 'name', title: '名称', width: 160 },
  { colKey: 'code', title: '编码', width: 160 },
  { colKey: 'tableName', title: '数据表', width: 180 },
  { colKey: 'status', title: '状态', width: 100 },
  { colKey: 'updatedAt', title: '更新时间', width: 180 },
  { colKey: 'operation', title: '操作', width: 220, fixed: 'right' },
];

async function fetchData() {
  loading.value = true;
  try {
    const res = await entityMetaApi.list({
      page: pagination.current,
      pageSize: pagination.pageSize,
      keyword: keyword.value || undefined,
    });
    if (res.data.code === 1) {
      const data = res.data.data;
      tableData.value = data.content || data.records || [];
      pagination.total = data.totalElements || data.total || 0;
    }
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.current = 1;
  fetchData();
}

function onPageChange(pageInfo: { current: number; pageSize: number }) {
  pagination.current = pageInfo.current;
  pagination.pageSize = pageInfo.pageSize;
  fetchData();
}

function handleCreate() {
  router.push('/lowcode/entity/new');
}

function handleEdit(row: any) {
  router.push(`/lowcode/entity/${row.id}`);
}

async function handlePublish(row: any) {
  try {
    const res = await entityMetaApi.publish(row.id);
    if (res.data.code === 1) {
      MessagePlugin.success(`实体 "${row.name}" 发布成功，数据表已创建`);
      fetchData();
    } else {
      MessagePlugin.error(res.data.msg || '发布失败');
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '发布失败');
  }
}

async function handleArchive(row: any) {
  try {
    const res = await entityMetaApi.archive(row.id);
    if (res.data.code === 1) {
      MessagePlugin.success(`实体 "${row.name}" 已归档`);
      fetchData();
    } else {
      MessagePlugin.error(res.data.msg || '归档失败');
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '归档失败');
  }
}

async function handleDelete(row: any) {
  try {
    const res = await entityMetaApi.delete(row.id);
    if (res.data.code === 1) {
      MessagePlugin.success(`实体 "${row.name}" 已删除`);
      fetchData();
    } else {
      MessagePlugin.error(res.data.msg || '删除失败');
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '删除失败');
  }
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.entity-list-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.search-bar {
  margin-bottom: 16px;
  max-width: 360px;
}
</style>
