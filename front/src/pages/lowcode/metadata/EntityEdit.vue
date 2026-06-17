<template>
  <div class="entity-edit-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <t-space>
        <t-button variant="text" @click="goHome">
          <template #icon><HomeIcon /></template>
          返回主页
        </t-button>
        <t-button variant="text" @click="goBack">
          <template #icon><t-icon name="chevron-left" /></template>
          返回列表
        </t-button>
      </t-space>
      <h2>{{ isNew ? '新建实体' : '编辑实体' }}</h2>
      <t-space>
        <t-button @click="goBack">取消</t-button>
        <t-button theme="primary" :loading="saving" @click="handleSave">保存</t-button>
      </t-space>
    </div>

    <!-- 基本信息 -->
    <t-card title="基本信息" class="section-card">
      <t-form ref="formRef" :data="entityForm" :rules="entityRules" label-width="100px">
        <t-row :gutter="24">
          <t-col :span="6">
            <t-form-item label="实体编码" name="code">
              <t-input v-model="entityForm.code" placeholder="小写字母开头，如 customer" :disabled="!isNew" />
            </t-form-item>
          </t-col>
          <t-col :span="6">
            <t-form-item label="实体名称" name="name">
              <t-input v-model="entityForm.name" placeholder="如：客户" />
            </t-form-item>
          </t-col>
        </t-row>
        <t-form-item label="描述" name="description">
          <t-textarea v-model="entityForm.description" placeholder="可选，实体用途说明" :maxlength="512" />
        </t-form-item>
      </t-form>
    </t-card>

    <!-- 字段管理 -->
    <t-card title="字段管理" class="section-card" style="margin-top: 16px;">
      <template #actions>
        <t-button size="small" theme="primary" @click="addField">
          <template #icon><t-icon name="add" /></template>
          添加字段
        </t-button>
      </template>

      <t-table
        :data="fields"
        :columns="fieldColumns"
        row-key="sortOrder"
        :drag-sort="'row-handler'"
        stripe
        @drag-sort="onFieldDragSort"
      >
        <template #drag>
          <t-icon name="move" style="cursor: grab;" />
        </template>
        <template #code="{ row }">
          <t-input v-model="row.code" size="small" style="width: 130px" placeholder="字段编码" />
        </template>
        <template #name="{ row }">
          <t-input v-model="row.name" size="small" style="width: 130px" placeholder="字段名称" />
        </template>
        <template #length="{ row }">
          <t-input-number
            v-if="row.fieldType === 'VARCHAR'"
            v-model="row.length"
            size="small"
            style="width: 70px"
            :min="1"
            :max="4000"
            placeholder="长度"
          />
          <span v-else>-</span>
        </template>
        <template #precision="{ row }">
          <t-input-number
            v-if="row.fieldType === 'DECIMAL'"
            v-model="row.precision"
            size="small"
            style="width: 60px"
            :min="1"
            :max="65"
            placeholder="精度"
          />
          <span v-else>-</span>
        </template>
        <template #scale="{ row }">
          <t-input-number
            v-if="row.fieldType === 'DECIMAL'"
            v-model="row.scale"
            size="small"
            style="width: 60px"
            :min="0"
            :max="30"
            placeholder="小数"
          />
          <span v-else>-</span>
        </template>
        <template #fieldType="{ row }">
          <t-select
            v-model="row.fieldType"
            :options="fieldTypeOptions"
            size="small"
            style="width: 130px"
          />
        </template>
        <template #referenceEntityCode="{ row }">
          <t-select
            v-if="row.fieldType === 'REFERENCE'"
            v-model="row.referenceEntityCode"
            :options="publishedEntities"
            size="small"
            style="width: 130px"
            placeholder="选择关联实体"
            clearable
          />
          <span v-else>-</span>
        </template>
        <template #referenceDisplayFieldCode="{ row }">
          <t-select
            v-if="row.fieldType === 'REFERENCE' && row.referenceEntityCode"
            v-model="row.referenceDisplayFieldCode"
            :options="entityFieldsCache[row.referenceEntityCode] || []"
            size="small"
            style="width: 100px"
            placeholder="显示字段"
            clearable
          />
          <span v-else>-</span>
        </template>
        <template #nullable="{ row }">
          <t-switch v-model="row.nullable" size="small" />
        </template>
        <template #showInList="{ row }">
          <t-checkbox v-model="row.showInList" />
        </template>
        <template #showInForm="{ row }">
          <t-checkbox v-model="row.showInForm" />
        </template>
        <template #showInSearch="{ row }">
          <t-checkbox v-model="row.showInSearch" />
        </template>
        <template #operation="{ rowIndex }">
          <t-link theme="danger" @click="removeField(rowIndex)">删除</t-link>
        </template>
      </t-table>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { HomeIcon } from 'tdesign-icons-vue-next';
import entityMetaApi from '../../../api/lowcode/entityMeta';
import type { EntityMeta, FieldMeta, FieldType } from '../../../types/lowcode';

const router = useRouter();
const route = useRoute();

const entityId = computed(() => {
  const id = route.params.id;
  return id && id !== 'new' ? Number(id) : null;
});

const isNew = computed(() => !entityId.value);
const saving = ref(false);
const formRef = ref();

// 实体表单
const entityForm = reactive<EntityMeta>({
  code: '',
  name: '',
  description: '',
});

const entityRules = {
  code: [
    { required: true, message: '请输入实体编码', type: 'error' },
    { pattern: /^[a-z][a-z0-9_]*$/, message: '编码需小写字母开头，仅含字母数字下划线', type: 'error' },
  ],
  name: [{ required: true, message: '请输入实体名称', type: 'error' }],
};

// 字段列表
const fields = ref<FieldMeta[]>([]);

const fieldTypeOptions = [
  { label: '字符串 (VARCHAR)', value: 'VARCHAR' },
  { label: '整数 (INT)', value: 'INTEGER' },
  { label: '长整数 (BIGINT)', value: 'LONG' },
  { label: '浮点数 (DOUBLE)', value: 'DOUBLE' },
  { label: '布尔 (TINYINT)', value: 'BOOLEAN' },
  { label: '日期 (DATE)', value: 'DATE' },
  { label: '日期时间 (DATETIME)', value: 'DATETIME' },
  { label: '长文本 (TEXT)', value: 'TEXT' },
  { label: 'JSON', value: 'JSON' },
  { label: '金额 (DECIMAL)', value: 'DECIMAL' },
  { label: '关联字段 (REFERENCE)', value: 'REFERENCE' },
];

const fieldColumns: PrimaryTableCol[] = [
  { colKey: 'drag', title: '', width: 40 },
  { colKey: 'code', title: '字段编码', width: 150 },
  { colKey: 'name', title: '字段名称', width: 150 },
  { colKey: 'fieldType', title: '字段类型', width: 160 },
  { colKey: 'referenceEntityCode', title: '关联实体', width: 150 },
  { colKey: 'referenceDisplayFieldCode', title: '显示字段', width: 120 },
  { colKey: 'length', title: '长度', width: 80 },
  { colKey: 'precision', title: '精度', width: 70 },
  { colKey: 'scale', title: '小数位', width: 70 },
  { colKey: 'nullable', title: '可为空', width: 80 },
  { colKey: 'showInList', title: '列表显示', width: 90 },
  { colKey: 'showInForm', title: '表单显示', width: 90 },
  { colKey: 'showInSearch', title: '可搜索', width: 80 },
  { colKey: 'operation', title: '操作', width: 80, fixed: 'right' },
];

// 已发布的实体列表（用于 REFERENCE 字段选择）
const publishedEntities = ref<{ label: string; value: string }[]>([]);

// 加载已发布的实体列表
async function loadPublishedEntities() {
  try {
    const res = await entityMetaApi.list({ page: 1, pageSize: 1000 });
    if (res.data.code === 1) {
      publishedEntities.value = res.data.data.content
        .filter((e: EntityMeta) => e.status === 'published')
        .map((e: EntityMeta) => ({ label: e.name, value: e.code }));
    }
  } catch (e) {
    console.error('加载实体列表失败:', e);
  }
}

// 获取实体的字段列表（用于选择显示字段）
const entityFieldsCache = ref<Record<string, { label: string; value: string }[]>>({});

async function loadEntityFields(entityCode: string) {
  if (!entityCode || entityFieldsCache.value[entityCode]) return;
  try {
    const res = await entityMetaApi.getByCode(entityCode);
    if (res.data.code === 1) {
      entityFieldsCache.value[entityCode] = res.data.data.fields
        .filter((f: FieldMeta) => f.showInList)
        .map((f: FieldMeta) => ({ label: f.name, value: f.code }));
    }
  } catch (e) {
    console.error('加载实体字段失败:', e);
  }
}

// 监听 REFERENCE 字段的关联实体变化
watch(fields, (newFields) => {
  newFields.forEach(f => {
    if (f.fieldType === 'REFERENCE' && f.referenceEntityCode) {
      loadEntityFields(f.referenceEntityCode);
    }
  });
}, { deep: true });

function addField() {
  const idx = fields.value.length + 1;
  fields.value.push({
    code: `field_${idx}`,
    name: `字段${idx}`,
    columnName: `field_${idx}`,
    fieldType: 'VARCHAR' as FieldType,
    length: 255,
    precision: 10,
    scale: 2,
    nullable: true,
    isPrimaryKey: false,
    isAutoIncrement: false,
    sortOrder: fields.value.length,
    showInList: true,
    showInForm: true,
    showInSearch: false,
  });
}

function removeField(index: number) {
  fields.value.splice(index, 1);
}

function onFieldDragSort({ newData }: { newData: any[] }) {
  fields.value = newData.map((f, i) => ({ ...f, sortOrder: i }));
}

async function handleSave() {
  const valid = await formRef.value?.validate();
  if (valid !== true) return;

  saving.value = true;
  try {
    if (isNew.value) {
      // 创建实体
      const res = await entityMetaApi.create(entityForm);
      if (res.data.code !== 1) {
        MessagePlugin.error(res.data.msg || '创建失败');
        return;
      }
      const created = res.data.data;
      // 保存字段
      if (fields.value.length > 0) {
        await entityMetaApi.updateFields(created.id, fields.value);
      }
      MessagePlugin.success('实体创建成功');
    } else {
      // 更新实体
      const res = await entityMetaApi.update(entityId.value!, entityForm);
      if (res.data.code !== 1) {
        MessagePlugin.error(res.data.msg || '更新失败');
        return;
      }
      // 保存字段
      await entityMetaApi.updateFields(entityId.value!, fields.value);
      MessagePlugin.success('实体更新成功');
    }
    router.push('/lowcode/entity');
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '保存失败');
  } finally {
    saving.value = false;
  }
}

function goBack() {
  router.push('/lowcode/entity');
}

function goHome() {
  router.push('/home');
}

onMounted(async () => {
  // 加载已发布的实体列表
  loadPublishedEntities();
  
  if (!isNew.value) {
    // 加载已有实体
    const res = await entityMetaApi.getById(entityId.value!);
    if (res.data.code === 1) {
      const { entity, fields: fieldList } = res.data.data;
      Object.assign(entityForm, entity);
      fields.value = fieldList || [];
      // 加载 REFERENCE 字段的关联实体字段
      fields.value.forEach(f => {
        if (f.fieldType === 'REFERENCE' && f.referenceEntityCode) {
          loadEntityFields(f.referenceEntityCode);
        }
      });
    }
  }
});
</script>

<style scoped>
.entity-edit-page {
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  flex: 1;
  font-size: 20px;
  font-weight: 600;
}

.section-card {
  border-radius: 8px;
}
</style>
