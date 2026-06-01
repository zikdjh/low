<template>
  <div class="data-list-page">
    <div class="page-header">
      <div class="header-left">
        <t-button variant="text" @click="goBack">
          <template #icon><t-icon name="chevron-left" /></template>
        </t-button>
        <h2>{{ entityMeta?.name || '数据管理' }}</h2>
        <t-tag v-if="entityMeta" theme="primary" variant="light">{{ entityMeta.code }}</t-tag>
      </div>
      <t-space>
        <t-button theme="primary" @click="handleCreate">
          <template #icon><t-icon name="add" /></template>
          新增数据
        </t-button>
      </t-space>
    </div>

    <div v-if="searchFields.length > 0" class="search-bar">
      <t-row :gutter="16">
        <t-col v-for="field in searchFields" :key="field.code" :span="3">
          <t-input
            v-model="searchForm[field.code]"
            :placeholder="`搜索${field.name}`"
            clearable
            @enter="fetchData"
            @clear="fetchData"
          />
        </t-col>
        <t-col :span="2">
          <t-button theme="primary" @click="fetchData">
            <template #icon><t-icon name="search" /></template>
            搜索
          </t-button>
        </t-col>
      </t-row>
    </div>

    <t-table
      :data="tableData"
      :columns="tableColumns"
      :loading="loading"
      :pagination="pagination"
      row-key="id"
      stripe
      hover
      @page-change="onPageChange"
    >
      <template #operation="{ row }">
        <t-space>
          <t-link theme="primary" @click="handleEdit(row)">编辑</t-link>
          <t-popconfirm content="确认删除该记录？" @confirm="handleDelete(row)">
            <t-link theme="danger">删除</t-link>
          </t-popconfirm>
        </t-space>
      </template>
    </t-table>

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
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import type { PrimaryTableCol } from 'tdesign-vue-next';
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

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
});

const searchForm = reactive<Record<string, string>>({});

const searchFields = computed(() => allFields.value.filter(f => f.showInSearch));
const listFields = computed(() => allFields.value.filter(f => f.showInList));
const formFields = computed(() => allFields.value.filter(f => f.showInForm));

const tableColumns = computed<PrimaryTableCol[]>(() => {
  const cols: PrimaryTableCol[] = [
    { colKey: 'id', title: 'ID', width: 80, ellipsis: true },
  ];
  listFields.value.forEach(field => {
    cols.push({
      colKey: field.code,
      title: field.name,
      width: getFieldWidth(field),
      ellipsis: true,
    });
  });
  cols.push({ colKey: 'createdAt', title: '创建时间', width: 160 });
  cols.push({ colKey: 'operation', title: '操作', width: 120, fixed: 'right' });
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

const dialogVisible = ref(false);
const dialogTitle = ref('新增数据');
const formData = reactive<Record<string, any>>({});
const formRef = ref();
const saving = ref(false);
const editingId = ref<number | null>(null);

async function loadEntityMeta() {
  try {
    const res = await entityMetaApi.getByCode(entityCode.value);
    if (res.data.code === 1) {
      const { entity, fields } = res.data.data;
      entityMeta.value = entity;
      allFields.value = fields || [];
    } else {
      MessagePlugin.error('加载实体元数据失败');
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '加载实体元数据失败');
  }
}

async function fetchData() {
  if (!entityMeta.value) return;
  loading.value = true;
  try {
    const filters: Record<string, string> = {};
    searchFields.value.forEach(f => {
      if (searchForm[f.code]) {
        filters[f.code] = searchForm[f.code];
      }
    });

    const res = await dynamicDataApi.list(entityCode.value, {
      page: pagination.current,
      pageSize: pagination.pageSize,
      filters,
    });

    if (res.data.code === 1) {
      const data = res.data.data;
      tableData.value = data.content || data.records || data.list || [];
      pagination.total = data.totalElements || data.total || 0;
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '加载数据失败');
  } finally {
    loading.value = false;
  }
}

function onPageChange(pageInfo: { current: number; pageSize: number }) {
  pagination.current = pageInfo.current;
  pagination.pageSize = pageInfo.pageSize;
  fetchData();
}

function handleCreate() {
  editingId.value = null;
  dialogTitle.value = '新增数据';
  Object.keys(formData).forEach(key => delete formData[key]);
  allFields.value.forEach(f => {
    if (f.defaultValue) {
      formData[f.code] = f.defaultValue;
    } else if (f.fieldType === 'BOOLEAN') {
      formData[f.code] = false;
    } else {
      formData[f.code] = null;
    }
  });
  dialogVisible.value = true;
}

function handleEdit(row: any) {
  editingId.value = row.id;
  dialogTitle.value = '编辑数据';
  Object.keys(formData).forEach(key => delete formData[key]);
  Object.assign(formData, row);
  dialogVisible.value = true;
}

async function handleSave() {
  const valid = await formRef.value?.validate();
  if (valid !== true) return;

  saving.value = true;
  try {
    if (editingId.value) {
      const res = await dynamicDataApi.update(entityCode.value, editingId.value, formData);
      if (res.data.code === 1) {
        MessagePlugin.success('更新成功');
        dialogVisible.value = false;
        fetchData();
      } else {
        MessagePlugin.error(res.data.msg || '更新失败');
      }
    } else {
      const res = await dynamicDataApi.create(entityCode.value, formData);
      if (res.data.code === 1) {
        MessagePlugin.success('创建成功');
        dialogVisible.value = false;
        fetchData();
      } else {
        MessagePlugin.error(res.data.msg || '创建失败');
      }
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '保存失败');
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

function goBack() {
  router.push('/lowcode/entity');
}

onMounted(async () => {
  await loadEntityMeta();
  await fetchData();
});
</script>

<style scoped>
.data-list-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.search-bar {
  margin-bottom: 16px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
}
</style>
