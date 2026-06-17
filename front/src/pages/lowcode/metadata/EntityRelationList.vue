<template>
  <div class="relation-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">实体关系管理</h2>
        <p class="page-subtitle">管理实体间的关联关系</p>
      </div>
      <div class="header-right">
        <t-button variant="text" @click="goBack">
          <template #icon><t-icon name="chevron-left" /></template>
          返回实体列表
        </t-button>
        <t-button theme="primary" @click="openCreateDialog">
          <template #icon><t-icon name="add" /></template>
          新建关系
        </t-button>
      </div>
    </div>

    <!-- 关系列表 -->
    <t-card class="table-card">
      <t-table
        :data="relationList"
        :columns="columns"
        :loading="loading"
        row-key="id"
        stripe
      >
        <template #relationType="{ row }">
          <t-tag :theme="getRelationTypeTheme(row.relationType)" variant="light" size="small">
            {{ getRelationTypeText(row.relationType) }}
          </t-tag>
        </template>
        <template #enabled="{ row }">
          <t-switch
            :value="row.enabled"
            size="small"
            @change="(val: boolean) => handleToggle(row, val)"
          />
        </template>
        <template #operation="{ row }">
          <t-space :size="4">
            <t-button size="small" variant="text" @click="openEditDialog(row)">
              <template #icon><t-icon name="edit" /></template>
              编辑
            </t-button>
            <t-popconfirm
              content="确认删除此关系？"
              @confirm="handleDelete(row.id)"
            >
              <t-button size="small" variant="text" theme="danger">
                <template #icon><t-icon name="delete" /></template>
                删除
              </t-button>
            </t-popconfirm>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <!-- 新建/编辑对话框 -->
    <t-dialog
      v-model:visible="dialogVisible"
      :header="isEditing ? '编辑关系' : '新建关系'"
      width="560px"
      :confirm-btn="{ content: '确定', loading: saving }"
      @confirm="handleSave"
      @close="resetForm"
    >
      <t-form ref="formRef" :data="formData" :rules="formRules" label-width="100px">
        <t-form-item label="源实体" name="sourceEntityCode">
          <t-select
            v-model="formData.sourceEntityCode"
            :options="entityOptions"
            placeholder="选择源实体"
            filterable
            clearable
            @change="onSourceEntityChange"
          />
        </t-form-item>
        <t-form-item label="关联字段" name="sourceFieldCode">
          <t-select
            v-model="formData.sourceFieldCode"
            :options="sourceFieldOptions"
            placeholder="选择源实体的关联字段"
            filterable
          />
        </t-form-item>
        <t-form-item label="关系类型" name="relationType">
          <t-select v-model="formData.relationType" :options="relationTypeOptions" />
        </t-form-item>
        <t-form-item label="目标实体" name="targetEntityCode">
          <t-select
            v-model="formData.targetEntityCode"
            :options="entityOptions"
            placeholder="选择目标实体"
            filterable
            clearable
            @change="onTargetEntityChange"
          />
        </t-form-item>
        <t-form-item label="显示字段" name="targetDisplayFieldCode">
          <t-select
            v-model="formData.targetDisplayFieldCode"
            :options="targetFieldOptions"
            placeholder="选择目标实体的显示字段（可选）"
            filterable
            clearable
          />
        </t-form-item>
        <t-form-item label="描述" name="description">
          <t-textarea v-model="formData.description" placeholder="关系说明（可选）" :maxlength="256" />
        </t-form-item>
        <t-form-item label="级联删除">
          <t-switch v-model="formData.cascadeDelete" />
        </t-form-item>
      </t-form>
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import type { PrimaryTableCol } from 'tdesign-vue-next';
import entityRelationApi from '../../../api/lowcode/entityRelation';
import entityMetaApi from '../../../api/lowcode/entityMeta';
import type { EntityRelation, EntityMeta, FieldMeta } from '../../../types/lowcode';

const router = useRouter();

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const isEditing = ref(false);
const editingId = ref<number | null>(null);
const relationList = ref<EntityRelation[]>([]);
const formRef = ref();

// 实体下拉选项
const entityOptions = ref<{ label: string; value: string }[]>([]);
const sourceFieldOptions = ref<{ label: string; value: string }[]>([]);
const targetFieldOptions = ref<{ label: string; value: string }[]>([]);

const formData = reactive<EntityRelation>({
  sourceEntityCode: '',
  sourceFieldCode: '',
  targetEntityCode: '',
  targetDisplayFieldCode: '',
  relationType: 'ONE_TO_MANY',
  description: '',
  cascadeDelete: false,
  enabled: true,
});

const formRules = {
  sourceEntityCode: [{ required: true, message: '请选择源实体' }],
  sourceFieldCode: [{ required: true, message: '请选择关联字段' }],
  targetEntityCode: [{ required: true, message: '请选择目标实体' }],
  relationType: [{ required: true, message: '请选择关系类型' }],
};

const relationTypeOptions = [
  { label: '一对多 (One-to-Many)', value: 'ONE_TO_MANY' },
  { label: '多对一 (Many-to-One)', value: 'MANY_TO_ONE' },
  { label: '一对一 (One-to-One)', value: 'ONE_TO_ONE' },
  { label: '多对多 (Many-to-Many)', value: 'MANY_TO_MANY' },
];

const columns: PrimaryTableCol[] = [
  { colKey: 'sourceEntityCode', title: '源实体', width: 140, ellipsis: true },
  { colKey: 'sourceFieldCode', title: '关联字段', width: 130, ellipsis: true },
  { colKey: 'relationType', title: '关系类型', width: 130 },
  { colKey: 'targetEntityCode', title: '目标实体', width: 140, ellipsis: true },
  { colKey: 'targetDisplayFieldCode', title: '显示字段', width: 110, ellipsis: true },
  { colKey: 'description', title: '描述', width: 160, ellipsis: true },
  { colKey: 'enabled', title: '启用', width: 70 },
  { colKey: 'operation', title: '操作', width: 140, fixed: 'right' },
];

function getRelationTypeText(type: string) {
  const map: Record<string, string> = {
    ONE_TO_ONE: '一对一',
    ONE_TO_MANY: '一对多',
    MANY_TO_ONE: '多对一',
    MANY_TO_MANY: '多对多',
  };
  return map[type] || type;
}

function getRelationTypeTheme(type: string) {
  const map: Record<string, string> = {
    ONE_TO_ONE: 'success',
    ONE_TO_MANY: 'primary',
    MANY_TO_ONE: 'warning',
    MANY_TO_MANY: 'danger',
  };
  return map[type] || 'default';
}

async function loadEntities() {
  try {
    const res = await entityMetaApi.list({ page: 1, pageSize: 1000 });
    if (res.data.code === 1) {
      const published = res.data.data.content
        .filter((e: EntityMeta) => e.status === 'published')
        .map((e: EntityMeta) => ({ label: `${e.name} (${e.code})`, value: e.code }));
      entityOptions.value = published;
    }
  } catch (e) {
    console.error('加载实体列表失败:', e);
  }
}

async function loadEntityFields(entityCode: string): Promise<{ label: string; value: string }[]> {
  try {
    const res = await entityMetaApi.getByCode(entityCode);
    if (res.data.code === 1) {
      return res.data.data.fields
        .filter((f: FieldMeta) => f.showInList)
        .map((f: FieldMeta) => ({ label: `${f.name} (${f.code})`, value: f.code }));
    }
  } catch (e) {
    console.error('加载字段失败:', e);
  }
  return [];
}

async function onSourceEntityChange(code: string) {
  formData.sourceFieldCode = '';
  if (code) {
    sourceFieldOptions.value = await loadEntityFields(code);
  } else {
    sourceFieldOptions.value = [];
  }
}

async function onTargetEntityChange(code: string) {
  formData.targetDisplayFieldCode = '';
  if (code) {
    targetFieldOptions.value = await loadEntityFields(code);
  } else {
    targetFieldOptions.value = [];
  }
}

async function fetchRelations() {
  loading.value = true;
  try {
    const res = await entityRelationApi.listAll();
    if (res.data.code === 1) {
      relationList.value = res.data.data || [];
    }
  } catch (e) {
    console.error('加载关系列表失败:', e);
  } finally {
    loading.value = false;
  }
}

function openCreateDialog() {
  isEditing.value = false;
  editingId.value = null;
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(row: EntityRelation) {
  isEditing.value = true;
  editingId.value = row.id || null;
  Object.assign(formData, {
    sourceEntityCode: row.sourceEntityCode,
    sourceFieldCode: row.sourceFieldCode,
    targetEntityCode: row.targetEntityCode,
    targetDisplayFieldCode: row.targetDisplayFieldCode || '',
    relationType: row.relationType,
    description: row.description || '',
    cascadeDelete: row.cascadeDelete || false,
    enabled: row.enabled,
  });
  // 预加载字段选项
  onSourceEntityChange(row.sourceEntityCode);
  onTargetEntityChange(row.targetEntityCode);
  dialogVisible.value = true;
}

function resetForm() {
  Object.assign(formData, {
    sourceEntityCode: '',
    sourceFieldCode: '',
    targetEntityCode: '',
    targetDisplayFieldCode: '',
    relationType: 'ONE_TO_MANY',
    description: '',
    cascadeDelete: false,
    enabled: true,
  });
  sourceFieldOptions.value = [];
  targetFieldOptions.value = [];
  formRef.value?.clearValidate();
}

async function handleSave() {
  const valid = await formRef.value?.validate();
  if (valid !== true) return;

  saving.value = true;
  try {
    const data: EntityRelation = {
      sourceEntityCode: formData.sourceEntityCode,
      sourceFieldCode: formData.sourceFieldCode,
      targetEntityCode: formData.targetEntityCode,
      targetDisplayFieldCode: formData.targetDisplayFieldCode || undefined,
      relationType: formData.relationType,
      description: formData.description,
      cascadeDelete: formData.cascadeDelete,
      enabled: formData.enabled,
    };

    let res;
    if (isEditing.value && editingId.value) {
      res = await entityRelationApi.update(editingId.value, data);
    } else {
      res = await entityRelationApi.create(data);
    }

    if (res.data.code === 1) {
      MessagePlugin.success(isEditing.value ? '关系更新成功' : '关系创建成功');
      dialogVisible.value = false;
      fetchRelations();
    } else {
      MessagePlugin.error(res.data.msg || '保存失败');
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '保存失败');
  } finally {
    saving.value = false;
  }
}

async function handleDelete(id: number) {
  try {
    const res = await entityRelationApi.delete(id);
    if (res.data.code === 1) {
      MessagePlugin.success('关系已删除');
      fetchRelations();
    } else {
      MessagePlugin.error(res.data.msg || '删除失败');
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '删除失败');
  }
}

async function handleToggle(row: EntityRelation, enabled: boolean) {
  try {
    const res = enabled
      ? await entityRelationApi.enable(row.id!)
      : await entityRelationApi.disable(row.id!);
    if (res.data.code === 1) {
      row.enabled = enabled;
      MessagePlugin.success(enabled ? '已启用' : '已禁用');
    } else {
      MessagePlugin.error(res.data.msg || '操作失败');
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '操作失败');
  }
}

function goBack() {
  router.push('/lowcode/entity');
}

onMounted(() => {
  loadEntities();
  fetchRelations();
});
</script>

<style scoped lang="less">
.relation-list-page {
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

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

.table-card {
  margin: 24px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f1f5f9;
}
</style>
