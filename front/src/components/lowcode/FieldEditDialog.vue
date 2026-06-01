<template>
  <t-dialog
    v-model:visible="visible"
    header="字段配置"
    width="600px"
    :confirm-btn="{ content: '确定', loading: saving }"
    @confirm="handleConfirm"
    @close="handleClose"
  >
    <t-form ref="formRef" :data="formData" :rules="rules" label-width="100px">
      <t-row :gutter="16">
        <t-col :span="6">
          <t-form-item label="字段编码" name="code">
            <t-input v-model="formData.code" placeholder="如: user_name" />
          </t-form-item>
        </t-col>
        <t-col :span="6">
          <t-form-item label="字段名称" name="name">
            <t-input v-model="formData.name" placeholder="如: 用户名" />
          </t-form-item>
        </t-col>
      </t-row>

      <t-row :gutter="16">
        <t-col :span="6">
          <t-form-item label="字段类型" name="fieldType">
            <t-select v-model="formData.fieldType" :options="fieldTypeOptions" />
          </t-form-item>
        </t-col>
        <t-col v-if="needsLength" :span="3">
          <t-form-item label="长度" name="length">
            <t-input-number v-model="formData.length" :min="1" :max="65535" />
          </t-form-item>
        </t-col>
        <t-col v-if="needsPrecision" :span="3">
          <t-form-item label="精度" name="precision">
            <t-input-number v-model="formData.precision" :min="1" :max="65" />
          </t-form-item>
        </t-col>
        <t-col v-if="needsScale" :span="3">
          <t-form-item label="标度" name="scale">
            <t-input-number v-model="formData.scale" :min="0" :max="30" />
          </t-form-item>
        </t-col>
      </t-row>

      <t-row :gutter="16">
        <t-col :span="4">
          <t-form-item label="可为空">
            <t-switch v-model="formData.nullable" />
          </t-form-item>
        </t-col>
        <t-col :span="4">
          <t-form-item label="主键">
            <t-switch v-model="formData.isPrimaryKey" />
          </t-form-item>
        </t-col>
        <t-col :span="4">
          <t-form-item label="自增">
            <t-switch v-model="formData.isAutoIncrement" :disabled="!formData.isPrimaryKey" />
          </t-form-item>
        </t-col>
      </t-row>

      <t-divider>显示设置</t-divider>

      <t-row :gutter="16">
        <t-col :span="4">
          <t-form-item label="列表显示">
            <t-checkbox v-model="formData.showInList" />
          </t-form-item>
        </t-col>
        <t-col :span="4">
          <t-form-item label="表单显示">
            <t-checkbox v-model="formData.showInForm" />
          </t-form-item>
        </t-col>
        <t-col :span="4">
          <t-form-item label="可搜索">
            <t-checkbox v-model="formData.showInSearch" />
          </t-form-item>
        </t-col>
      </t-row>

      <t-form-item label="默认值" name="defaultValue">
        <t-input v-model="formData.defaultValue" placeholder="可选" />
      </t-form-item>

      <t-form-item label="校验规则" name="validationRule">
        <t-textarea
          v-model="formData.validationRule"
          placeholder='JSON格式，如: {"min":1,"max":100}'
          :autosize="{ minRows: 2, maxRows: 4 }"
        />
      </t-form-item>
    </t-form>
  </t-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue';
import type { FieldMeta, FieldType } from '../../types/lowcode';

const props = defineProps<{
  modelValue: boolean;
  field?: FieldMeta | null;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'confirm', field: FieldMeta): void;
}>();

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
});

const formRef = ref();
const saving = ref(false);

const defaultFormData = (): FieldMeta => ({
  code: '',
  name: '',
  columnName: '',
  fieldType: 'VARCHAR' as FieldType,
  length: 255,
  precision: 10,
  scale: 2,
  nullable: true,
  defaultValue: '',
  isPrimaryKey: false,
  isAutoIncrement: false,
  sortOrder: 0,
  showInList: true,
  showInForm: true,
  showInSearch: false,
});

const formData = reactive<FieldMeta>(defaultFormData());

const fieldTypeOptions = [
  { label: '字符串 (VARCHAR)', value: 'VARCHAR' },
  { label: '整数 (INT)', value: 'INTEGER' },
  { label: '长整数 (BIGINT)', value: 'LONG' },
  { label: '浮点数 (DOUBLE)', value: 'DOUBLE' },
  { label: '金额 (DECIMAL)', value: 'DECIMAL' },
  { label: '布尔 (BOOLEAN)', value: 'BOOLEAN' },
  { label: '日期 (DATE)', value: 'DATE' },
  { label: '日期时间 (DATETIME)', value: 'DATETIME' },
  { label: '长文本 (TEXT)', value: 'TEXT' },
  { label: 'JSON', value: 'JSON' },
];

const needsLength = computed(() => formData.fieldType === 'VARCHAR');
const needsPrecision = computed(() => formData.fieldType === 'DECIMAL');
const needsScale = computed(() => formData.fieldType === 'DECIMAL');

const rules = {
  code: [
    { required: true, message: '请输入字段编码', type: 'error' },
    { pattern: /^[a-z][a-z0-9_]*$/, message: '编码需小写字母开头', type: 'error' },
  ],
  name: [{ required: true, message: '请输入字段名称', type: 'error' }],
  fieldType: [{ required: true, message: '请选择字段类型', type: 'error' }],
};

watch(() => props.field, (newField) => {
  if (newField) {
    Object.assign(formData, newField);
  } else {
    Object.assign(formData, defaultFormData());
  }
}, { immediate: true });

watch(() => formData.fieldType, (newType) => {
  if (newType === 'VARCHAR') {
    formData.length = 255;
  } else if (newType === 'DECIMAL') {
    formData.precision = 10;
    formData.scale = 2;
  }
});

async function handleConfirm() {
  const valid = await formRef.value?.validate();
  if (valid !== true) return;

  saving.value = true;
  try {
    formData.columnName = formData.code;
    emit('confirm', { ...formData });
    visible.value = false;
  } finally {
    saving.value = false;
  }
}

function handleClose() {
  formRef.value?.reset();
}
</script>
