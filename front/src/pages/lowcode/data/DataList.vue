<template>
  <div class="data-list-page">
    <div class="page-header">
      <div class="header-left">
        <BackButton to="/lowcode/entity" label="返回实体列表" class="back-btn-wrapper" />
        <div class="title-group">
          <h2 class="page-title">{{ entityMeta?.name || '数据管理' }}</h2>
          <p class="page-subtitle">管理 {{ entityMeta?.name }} 的数据记录</p>
        </div>
      </div>
      <div class="header-right">
        <t-button variant="outline" @click="handleExport">
          <template #icon><DownloadIcon size="16" /></template>
          导出数据
        </t-button>
        <t-button theme="primary" @click="handleCreate">
          <template #icon><PlusIcon size="16" /></template>
          新增数据
        </t-button>
      </div>
    </div>

    <t-card class="filter-card" v-if="searchFields.length > 0">
      <div class="filter-header">
        <FilterIcon size="16" />
        <span>筛选条件</span>
      </div>
      <div class="filter-content">
        <t-row :gutter="16">
          <t-col v-for="field in searchFields" :key="field.code" :span="6">
            <t-form-item :label="field.name" :label-width="80">
              <component
                :is="getSearchComponent(field)"
                v-model="searchForm[field.code]"
                :placeholder="`请输入${field.name}`"
                clearable
                size="small"
              />
            </t-form-item>
          </t-col>
        </t-row>
        <div class="filter-actions">
          <t-button variant="outline" @click="resetSearch" size="small">重置</t-button>
          <t-button theme="primary" @click="handleSearch" size="small">
            <template #icon><SearchIcon size="14" /></template>
            搜索
          </t-button>
        </div>
      </div>
    </t-card>

    <div class="stats-bar">
      <span class="stats-text">共 <strong>{{ pagination.total }}</strong> 条记录</span>
      <div class="stats-actions">
        <t-button
          v-if="selectedRows.length > 0"
          variant="outline"
          theme="danger"
          @click="handleBatchDelete"
          size="small"
        >
          <template #icon><DeleteIcon size="14" /></template>
          批量删除 ({{ selectedRows.length }})
        </t-button>
      </div>
    </div>

    <t-card class="table-card">
      <t-table
        :data="tableData"
        :columns="tableColumns"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        stripe
        hover
        show-header-overflow
        show-row-overflow
        :select-all="true"
        :selected-rows="selectedRows"
        @update:selected-rows="onSelectChange"
        @page-change="onPageChange"
        @sort-change="onSortChange"
      >
        <template #operation="{ row }">
          <t-space :size="4">
            <t-button
              size="small"
              variant="text"
              @click="handleView(row)"
            >
              <template #icon><KeyIcon size="14" /></template>
              查看
            </t-button>
            <t-button
              size="small"
              variant="text"
              theme="primary"
              @click="handleEdit(row)"
            >
              <template #icon><EditIcon size="14" /></template>
              编辑
            </t-button>
            <t-popconfirm
              content="确认删除该记录？此操作不可恢复。"
              @confirm="handleDelete(row)"
            >
              <t-button size="small" theme="danger">
                <template #icon><DeleteIcon size="14" /></template>
                删除
              </t-button>
            </t-popconfirm>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <t-dialog
      v-model:visible="viewDialogVisible"
      :header="`查看 ${entityMeta?.name}`"
      width="600px"
      :footer="false"
    >
      <div class="detail-content">
        <t-descriptions :column="2" border>
          <t-descriptions-item label="ID">{{ viewData.id }}</t-descriptions-item>
          <t-descriptions-item label="创建时间">{{ viewData.createdAt }}</t-descriptions-item>
          <t-descriptions-item label="更新时间">{{ viewData.updatedAt }}</t-descriptions-item>
          <template v-for="field in formFields" :key="field.code">
            <t-descriptions-item :label="field.name">
              <span v-if="viewData[field.code] !== undefined && viewData[field.code] !== null">
                {{ formatFieldValue(viewData[field.code], field) }}
              </span>
              <span v-else class="empty-value">-</span>
            </t-descriptions-item>
          </template>
        </t-descriptions>
      </div>
    </t-dialog>

    <t-dialog
      v-model:visible="dialogVisible"
      :header="dialogTitle"
      width="800px"
      :confirm-btn="{ content: '保存', loading: saving }"
      @confirm="handleSave"
    >
      <dynamic-form
        v-if="dialogVisible"
        ref="formRef"
        :fields="formFields"
        :data="formData"
        :readonly="false"
      />
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, markRaw } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { ChevronLeftIcon, DownloadIcon, PlusIcon, FilterIcon, SearchIcon, DeleteIcon, EditIcon, KeyIcon } from 'tdesign-icons-vue-next';
import BackButton from '../../../components/common/BackButton.vue';
import entityMetaApi from '../../../api/lowcode/entityMeta';
import dynamicDataApi from '../../../api/lowcode/dynamicData';
import DynamicForm from '../../../components/lowcode/DynamicForm.vue';
import type { EntityMeta, FieldMeta } from '../../../types/lowcode';

const router = useRouter();
const route = useRoute();

const entityCode = computed(() => route.params.entityCode as string);
const entityMeta = ref<EntityMeta | null>(null);
const allFields = ref<FieldMeta[]>([]);
const loading = ref(false);
const tableData = ref<any[]>([]);
const selectedRows = ref<any[]>([]);
const sortField = ref('');
const sortOrder = ref<'asc' | 'desc'>('desc');

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const searchForm = reactive<Record<string, any>>({});

const searchFields = computed(() => allFields.value.filter(f => f.showInSearch));
const listFields = computed(() => allFields.value.filter(f => f.showInList));
const formFields = computed(() => allFields.value.filter(f => f.showInForm));

const tableColumns = computed<PrimaryTableCol[]>(() => {
  const cols: PrimaryTableCol[] = [
    { type: 'multiple', width: 60 },
    { 
      colKey: 'id', 
      title: 'ID', 
      width: 80, 
      ellipsis: true,
      sorter: true,
    },
  ];
  listFields.value.forEach(field => {
    cols.push({
      colKey: field.code,
      title: field.name,
      width: getFieldWidth(field),
      ellipsis: true,
      sorter: (field.sortable !== false) as any,
    });
  });
  cols.push({ 
    colKey: 'createdAt', 
    title: '创建时间', 
    width: 160,
    sorter: true,
  });
  cols.push({ 
    colKey: 'operation', 
    title: '操作', 
    width: 220, 
    fixed: 'right',
  });
  return cols;
});

function getFieldWidth(field: FieldMeta): number {
  switch (field.fieldType) {
    case 'VARCHAR': return Math.min((field.length || 255) / 2 + 50, 300);
    case 'TEXT': return 300;
    case 'INTEGER':
    case 'LONG': return 100;
    case 'DOUBLE':
    case 'DECIMAL': return 120;
    case 'BOOLEAN': return 80;
    case 'DATE': return 120;
    case 'DATETIME': return 160;
    default: return 150;
  }
}

function getSearchComponent(field: FieldMeta) {
  // 延迟加载组件缓存，避免返回 Promise 导致渲染失败
  switch (field.fieldType) {
    case 'DATE':
    case 'DATETIME':
      return searchComponents.value.date;
    case 'BOOLEAN':
      return searchComponents.value.bool;
    default:
      return searchComponents.value.text;
  }
}

// 缓存动态加载的搜索组件
const searchComponents = ref({ text: null as any, date: null as any, bool: null as any });

import('tdesign-vue-next').then(m => {
  searchComponents.value = {
    text: markRaw(m.Input),
    date: markRaw(m.DatePicker),
    bool: markRaw(m.Select),
  };
});

function formatFieldValue(value: any, field: FieldMeta): string {
  if (value === null || value === undefined) return '-';
  
  switch (field.fieldType) {
    case 'BOOLEAN':
      return value ? '是' : '否';
    case 'DATE':
    case 'DATETIME':
      return formatDateTime(value);
    case 'DECIMAL':
      return Number(value).toFixed(field.decimalPlaces || 2);
    default:
      return String(value);
  }
}

function formatDateTime(value: string): string {
  if (!value) return '-';
  const date = new Date(value);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
}

const dialogVisible = ref(false);
const dialogTitle = ref('新增数据');
const formData = reactive<Record<string, any>>({});
const saving = ref(false);
const editingId = ref<number | null>(null);

const viewDialogVisible = ref(false);
const viewData = reactive<Record<string, any>>({});

async function loadEntityMeta() {
  try {
    const res = await entityMetaApi.getByCode(entityCode.value);
    if (res.data.code === 1) {
      entityMeta.value = res.data.data;
      allFields.value = res.data.data.fields || [];
      initSearchForm();
      fetchData();
    }
  } catch (e: any) {
    MessagePlugin.error('加载实体信息失败');
  }
}

function initSearchForm() {
  searchFields.value.forEach(field => {
    searchForm[field.code] = '';
  });
}

async function fetchData() {
  loading.value = true;
  try {
    const params: Record<string, any> = {
      page: pagination.current,
      pageSize: pagination.pageSize,
    };
    
    if (sortField.value) {
      params.sortBy = sortField.value;
      params.sortOrder = sortOrder.value;
    }
    
    Object.keys(searchForm).forEach(key => {
      if (searchForm[key]) {
        params[key] = searchForm[key];
      }
    });

    const res = await dynamicDataApi.list(entityCode.value, params);
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

function resetSearch() {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = '';
  });
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

function goBack() {
  router.push('/lowcode/entity');
}

function handleCreate() {
  dialogTitle.value = '新增数据';
  editingId.value = null;
  Object.keys(formData).forEach(key => delete formData[key]);
  formFields.value.forEach(field => {
    formData[field.code] = field.defaultValue || '';
  });
  dialogVisible.value = true;
}

function handleView(row: any) {
  Object.keys(viewData).forEach(key => delete viewData[key]);
  Object.assign(viewData, row);
  viewDialogVisible.value = true;
}

function handleEdit(row: any) {
  dialogTitle.value = '编辑数据';
  editingId.value = row.id;
  Object.keys(formData).forEach(key => delete formData[key]);
  Object.assign(formData, row);
  dialogVisible.value = true;
}

async function handleSave() {
  saving.value = true;
  try {
    let res;
    if (editingId.value) {
      res = await dynamicDataApi.update(entityCode.value, editingId.value, formData);
    } else {
      res = await dynamicDataApi.create(entityCode.value, formData);
    }
    
    if (res.data.code === 1) {
      MessagePlugin.success(editingId.value ? '修改成功' : '新增成功');
      dialogVisible.value = false;
      fetchData();
    } else {
      MessagePlugin.error(res.data.msg || '操作失败');
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '操作失败');
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: any) {
  try {
    const res = await dynamicDataApi.delete(entityCode.value, row.id);
    if (res.data.code === 1) {
      MessagePlugin.success('删除成功');
      fetchData();
    } else {
      MessagePlugin.error(res.data.msg || '删除失败');
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '删除失败');
  }
}

async function handleBatchDelete() {
  if (selectedRows.value.length === 0) {
    MessagePlugin.warning('请选择要删除的记录');
    return;
  }
  
  const ids = selectedRows.value.map(r => r.id);
  try {
    const res = await dynamicDataApi.batchDelete(entityCode.value, ids);
    if (res.data.code === 1) {
      MessagePlugin.success(`已成功删除 ${selectedRows.value.length} 条记录`);
      selectedRows.value = [];
      fetchData();
    } else {
      MessagePlugin.error(res.data.msg || '批量删除失败');
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '批量删除失败');
  }
}

async function handleExport() {
  try {
    // Client-side CSV export from current table data
    const visibleFields = allFields.value.filter(f => f.showInList);
    const headers = visibleFields.map(f => f.name).join(',');
    const rows = tableData.value.map((row: any) =>
      visibleFields.map(f => {
        const val = row[f.code];
        if (val === null || val === undefined) return '';
        const str = String(val);
        return str.includes(',') ? `"${str}"` : str;
      }).join(',')
    );
    const csv = [headers, ...rows].join('\n');
    const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${entityCode.value}_export_${Date.now()}.csv`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    MessagePlugin.success('导出成功');
  } catch (e: any) {
    MessagePlugin.error('导出失败');
  }
}

onMounted(() => {
  loadEntityMeta();
});
</script>

<style scoped lang="less">
.data-list-page {
  padding: 0;
  background: #f7f8fa;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .title-group {
      .page-title {
        font-size: 24px;
        font-weight: 700;
        color: #1a1a1a;
        margin: 0 0 8px 0;
      }
      .page-subtitle {
        font-size: 14px;
        color: #999;
        margin: 0;
      }
    }
  }
  
  .header-right {
    display: flex;
    gap: 12px;
  }
  
  .back-btn-wrapper {
    margin-right: 4px;
  }
}

.filter-card {
  margin: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  
  .filter-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #1a1a1a;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 16px;
    :deep(.t-icon) { color: var(--td-brand-color, #E8A317); }
  }
  
  .filter-content {
    .filter-actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #f0f0f0;
    }
  }
}

.stats-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px 16px;
  
  .stats-text {
    font-size: 14px;
    color: #777;
    strong { color: #1a1a1a; font-weight: 600; }
  }
  .stats-actions { display: flex; gap: 12px; }
}

.table-card {
  margin: 0 24px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.detail-content {
  .empty-value { color: #bbb; }
}
</style>
