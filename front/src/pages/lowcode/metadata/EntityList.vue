<template>
  <div class="entity-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">实体管理</h2>
        <p class="page-subtitle">管理业务实体和数据模型</p>
      </div>
      <div class="header-right">
        <t-button theme="primary" @click="handleCreate">
          <template #icon><PlusIcon /></template>
          新建实体
        </t-button>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <t-input
          v-model="keyword"
          placeholder="搜索实体名称或编码..."
          clearable
          :style="{ width: '280px' }"
          @enter="handleSearch"
          @clear="handleSearch"
        >
          <template #suffix-icon><SearchIcon /></template>
        </t-input>
        
        <t-select
          v-model="statusFilter"
          placeholder="全部状态"
          :style="{ width: '140px', marginLeft: '12px' }"
          @change="handleSearch"
        >
          <t-option value="" label="全部状态" />
          <t-option value="draft" label="草稿" />
          <t-option value="published" label="已发布" />
          <t-option value="archived" label="已归档" />
        </t-select>
      </div>
      
      <div class="toolbar-right" v-if="selectedRows.length > 0">
        <t-button
          variant="outline"
          @click="handleBatchArchive"
          :disabled="!canBatchArchive"
        >
          <template #icon><FolderIcon /></template>
          批量归档
        </t-button>
        <t-button
          variant="outline"
          theme="danger"
          @click="handleBatchDelete"
          :disabled="!canBatchDelete"
        >
          <template #icon><DeleteIcon /></template>
          批量删除
        </t-button>
        <span class="selected-count">已选择 {{ selectedRows.length }} 项</span>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <t-card class="stat-card">
        <div class="stat-icon total">
          <t-icon name="database" size="24" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总实体数</div>
        </div>
      </t-card>
      <t-card class="stat-card">
        <div class="stat-icon draft">
          <t-icon name="file-edit" size="24" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.draft }}</div>
          <div class="stat-label">草稿</div>
        </div>
      </t-card>
      <t-card class="stat-card">
        <div class="stat-icon published">
          <t-icon name="check-circle" size="24" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.published }}</div>
          <div class="stat-label">已发布</div>
        </div>
      </t-card>
      <t-card class="stat-card">
        <div class="stat-icon archived">
          <t-icon name="archive" size="24" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.archived }}</div>
          <div class="stat-label">已归档</div>
        </div>
      </t-card>
    </div>

    <!-- 实体列表 -->
    <t-card class="table-card">
      <t-table
        :data="tableData"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        stripe
        show-header-overflow
        show-row-overflow
        :select-all="true"
        :selected-rows="selectedRows"
        @update:selected-rows="onSelectChange"
        @page-change="onPageChange"
        @sort-change="onSortChange"
      >
        <template #status="{ row }">
          <t-tag 
            :theme="getStatusTheme(row.status)" 
            variant="light"
            size="small"
          >
            {{ getStatusText(row.status) }}
          </t-tag>
        </template>
        <template #tableName="{ row }">
          <code class="table-name">{{ row.tableName }}</code>
        </template>
        <template #fieldCount="{ row }">
          <span class="field-badge">{{ row.fieldCount || 0 }} 个字段</span>
        </template>
        <template #operation="{ row }">
          <t-space :size="4">
            <t-button
              size="small"
              variant="text"
              @click="handleEdit(row)"
            >
              <template #icon><t-icon name="edit" /></template>
              编辑
            </t-button>
            <t-button
              v-if="row.status === 'published'"
              size="small"
              variant="text"
              theme="primary"
              @click="handleDataManage(row)"
            >
              <template #icon><DataBaseIcon /></template>
              数据管理
            </t-button>
            <t-button
              v-if="row.status === 'draft'"
              size="small"
              theme="success"
              @click="handlePublish(row)"
            >
              <template #icon><CheckCircleIcon /></template>
              发布
            </t-button>
            <t-button
              v-if="row.status === 'published'"
              size="small"
              theme="warning"
              @click="handleArchive(row)"
            >
              <template #icon><FolderIcon /></template>
              归档
            </t-button>
            <t-popconfirm
              v-if="row.status === 'draft'"
              content="确认删除该实体？此操作不可恢复。"
              @confirm="handleDelete(row)"
            >
              <t-button size="small" theme="danger">
                <template #icon><DeleteIcon /></template>
                删除
              </t-button>
            </t-popconfirm>
          </t-space>
        </template>
      </t-table>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { PlusIcon, SearchIcon, DeleteIcon, DataBaseIcon, CheckCircleIcon, FolderIcon } from 'tdesign-icons-vue-next';
import entityMetaApi from '../../../api/lowcode/entityMeta';

const router = useRouter();

const loading = ref(false);
const keyword = ref('');
const statusFilter = ref<string>('');
const tableData = ref<any[]>([]);
const selectedRows = ref<any[]>([]);
const sortField = ref('');
const sortOrder = ref<'asc' | 'desc'>('desc');

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const stats = reactive({
  total: 0,
  draft: 0,
  published: 0,
  archived: 0,
});

const canBatchArchive = computed(() => {
  return selectedRows.value.some(row => row.status === 'published');
});

const canBatchDelete = computed(() => {
  return selectedRows.value.some(row => row.status === 'draft');
});

const columns: PrimaryTableCol[] = [
  { type: 'multiple', width: 60 },
  { 
    colKey: 'name', 
    title: '实体名称', 
    width: 160,
    sorter: true,
    ellipsis: true,
  },
  { 
    colKey: 'code', 
    title: '实体编码', 
    width: 160,
    sorter: true,
    ellipsis: true,
  },
  { 
    colKey: 'tableName', 
    title: '数据表', 
    width: 180,
    ellipsis: true,
  },
  { 
    colKey: 'fieldCount', 
    title: '字段数量', 
    width: 100,
  },
  { 
    colKey: 'status', 
    title: '状态', 
    width: 100,
  },
  { 
    colKey: 'updatedAt', 
    title: '更新时间', 
    width: 180,
    sorter: true,
  },
  { 
    colKey: 'operation', 
    title: '操作', 
    width: 280, 
    fixed: 'right',
  },
];

function getStatusText(status: string) {
  const map: Record<string, string> = {
    draft: '草稿',
    published: '已发布',
    archived: '已归档',
  };
  return map[status] || status;
}

function getStatusTheme(status: string) {
  const map: Record<string, string> = {
    draft: 'default',
    published: 'success',
    archived: 'warning',
  };
  return map[status] || 'default';
}

async function fetchData() {
  loading.value = true;
  try {
    const res = await entityMetaApi.list({
      page: pagination.current,
      pageSize: pagination.pageSize,
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
      sortBy: sortField.value || undefined,
      sortOrder: sortOrder.value || undefined,
    });
    if (res.data.code === 1) {
      const data = res.data.data;
      tableData.value = data.content || data.records || [];
      pagination.total = data.totalElements || data.total || 0;
      updateStats();
    }
  } finally {
    loading.value = false;
  }
}

function updateStats() {
  stats.total = tableData.value.length;
  stats.draft = tableData.value.filter(r => r.status === 'draft').length;
  stats.published = tableData.value.filter(r => r.status === 'published').length;
  stats.archived = tableData.value.filter(r => r.status === 'archived').length;
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

function onSortChange(sortInfo: { field: string; order: 'asc' | 'desc' }) {
  sortField.value = sortInfo.field;
  sortOrder.value = sortInfo.order;
  fetchData();
}

function onSelectChange(rows: any[]) {
  selectedRows.value = rows;
}

function handleCreate() {
  router.push('/lowcode/entity/new');
}

function handleEdit(row: any) {
  router.push(`/lowcode/entity/${row.id}`);
}

function handleDataManage(row: any) {
  router.push(`/lowcode/data/${row.code}`);
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

async function handleBatchArchive() {
  const publishRows = selectedRows.value.filter(r => r.status === 'published');
  if (publishRows.length === 0) {
    MessagePlugin.warning('请选择已发布的实体');
    return;
  }
  
  try {
    for (const row of publishRows) {
      await entityMetaApi.archive(row.id);
    }
    MessagePlugin.success(`已成功归档 ${publishRows.length} 个实体`);
    selectedRows.value = [];
    fetchData();
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '批量归档失败');
  }
}

async function handleBatchDelete() {
  const draftRows = selectedRows.value.filter(r => r.status === 'draft');
  if (draftRows.length === 0) {
    MessagePlugin.warning('请选择草稿状态的实体');
    return;
  }
  
  try {
    for (const row of draftRows) {
      await entityMetaApi.delete(row.id);
    }
    MessagePlugin.success(`已成功删除 ${draftRows.length} 个实体`);
    selectedRows.value = [];
    fetchData();
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '批量删除失败');
  }
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="less">
.entity-list-page {
  padding: 24px;
  background: #f5f6f8;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  
  .header-left {
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #1f2329;
      margin: 0 0 8px 0;
    }
    
    .page-subtitle {
      font-size: 14px;
      color: #8f959e;
      margin: 0;
    }
  }
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  .selected-count {
    font-size: 14px;
    color: #646a73;
    background: #f2f3f5;
    padding: 6px 12px;
    border-radius: 4px;
  }
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  
  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
    
    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      
      &.total {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
      
      &.draft {
        background: #e8f4fd;
        color: #1677ff;
      }
      
      &.published {
        background: #e8fae8;
        color: #52c41a;
      }
      
      &.archived {
        background: #fff7e6;
        color: #fa8c16;
      }
    }
    
    .stat-info {
      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #1f2329;
        margin-bottom: 4px;
      }
      
      .stat-label {
        font-size: 14px;
        color: #8f959e;
      }
    }
  }
}

.table-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  
  :deep(.t-table) {
    .table-name {
      font-family: monospace;
      font-size: 12px;
      color: #1677ff;
      background: #e8f4fd;
      padding: 2px 8px;
      border-radius: 4px;
    }
    
    .field-badge {
      background: #f2f3f5;
      color: #646a73;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 12px;
    }
  }
}
</style>
