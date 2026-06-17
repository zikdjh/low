<template>
  <div class="entity-list-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">实体管理</h2>
        <p class="page-subtitle">管理业务实体和数据模型</p>
      </div>
      <div class="header-right">
        <t-button variant="outline" @click="goRelations">
          <template #icon><t-icon name="link" /></template>
          关系管理
        </t-button>
        <t-button theme="primary" @click="handleCreate">
          <template #icon><PlusIcon /></template>
          新建实体
        </t-button>
      </div>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <t-input
          v-model="keyword"
          placeholder="搜索实体名称或编码..."
          clearable
          :style="{ width: '320px' }"
          @enter="handleSearch"
          @clear="handleSearch"
        >
          <template #suffix-icon><SearchIcon /></template>
        </t-input>
        
        <t-select
          v-model="statusFilter"
          placeholder="全部状态"
          :style="{ width: '160px', marginLeft: '16px' }"
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

    <div class="stats-cards">
      <t-card class="stat-card">
        <div class="stat-icon total">
          <DataBaseIcon size="24" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总实体数</div>
        </div>
      </t-card>
      <t-card class="stat-card">
        <div class="stat-icon draft">
          <FileEditIcon size="24" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.draft }}</div>
          <div class="stat-label">草稿</div>
        </div>
      </t-card>
      <t-card class="stat-card">
        <div class="stat-icon published">
          <CheckCircleIcon size="24" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.published }}</div>
          <div class="stat-label">已发布</div>
        </div>
      </t-card>
      <t-card class="stat-card">
        <div class="stat-icon archived">
          <FolderIcon size="24" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.archived }}</div>
          <div class="stat-label">已归档</div>
        </div>
      </t-card>
    </div>

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
              <template #icon><EditIcon /></template>
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
import { 
  PlusIcon, SearchIcon, DeleteIcon, DataBaseIcon, CheckCircleIcon, 
  FolderIcon, FileEditIcon, EditIcon 
} from 'tdesign-icons-vue-next';
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
    width: 300, 
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
  stats.total = pagination.total;
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

function goRelations() {
  router.push('/lowcode/entity/relations');
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
  padding: 0;
  background: #f8fafc;
  min-height: calc(100vh - 70px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  
  .header-left {
    .page-title {
      font-size: 26px;
      font-weight: 700;
      color: #1e293b;
      margin: 0 0 8px 0;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    
    .page-subtitle {
      font-size: 14px;
      color: #64748b;
      margin: 0;
    }
  }
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  
  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  .selected-count {
    font-size: 13px;
    color: #64748b;
    background: #f1f5f9;
    padding: 6px 14px;
    border-radius: 20px;
    font-weight: 500;
  }
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 24px;
  
  .stat-card {
    display: flex;
    align-items: center;
    padding: 24px;
    background: #ffffff;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
    border: 1px solid #f1f5f9;
    
    .stat-icon {
      width: 52px;
      height: 52px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      
      &.total {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        
        :deep(.t-icon) {
          color: #fff;
        }
      }
      
      &.draft {
        background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
        
        :deep(.t-icon) {
          color: #3b82f6;
        }
      }
      
      &.published {
        background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
        
        :deep(.t-icon) {
          color: #22c55e;
        }
      }
      
      &.archived {
        background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
        
        :deep(.t-icon) {
          color: #f59e0b;
        }
      }
    }
    
    .stat-info {
      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: #1e293b;
        margin-bottom: 4px;
      }
      
      .stat-label {
        font-size: 14px;
        color: #64748b;
      }
    }
  }
}

.table-card {
  margin: 0 24px 24px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f1f5f9;
  
  :deep(.t-table) {
    .table-name {
      font-family: 'JetBrains Mono', 'Consolas', monospace;
      font-size: 12px;
      color: #3b82f6;
      background: #dbeafe;
      padding: 3px 10px;
      border-radius: 6px;
    }
    
    .field-badge {
      background: #f1f5f9;
      color: #64748b;
      padding: 3px 10px;
      border-radius: 6px;
      font-size: 12px;
      font-weight: 500;
    }
  }
}
</style>
