<template>
  <t-form ref="formRef" :data="modelValue" :rules="rules" label-width="120px">
    <t-row :gutter="24">
      <t-col v-for="field in fields" :key="field.code" :span="getFieldSpan(field)">
        <t-form-item :label="field.name" :name="field.code">
          <!-- REFERENCE 字段使用下拉选择 -->
          <t-select
            v-if="field.fieldType === 'REFERENCE'"
            v-model="modelValue[field.code]"
            :options="getReferenceOptions(field)"
            :loading="referenceLoading[field.referenceEntityCode || '']"
            :disabled="readonly"
            :placeholder="getPlaceholder(field)"
            clearable
            filterable
          />
          <!-- 其他字段类型 -->
          <component
            v-else
            :is="getInputComponent(field)"
            v-model="modelValue[field.code]"
            v-bind="getInputProps(field)"
            :disabled="readonly"
            :placeholder="getPlaceholder(field)"
          />
        </t-form-item>
      </t-col>
    </t-row>
  </t-form>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { ref, computed, onMounted } from 'vue';
import type { FieldMeta } from '../../types/lowcode';
import dynamicDataApi from '../../api/lowcode/dynamicData';

const props = defineProps<{
  fields: FieldMeta[];
  data: Record<string, any>;
  readonly?: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:data', value: Record<string, any>): void;
}>();

const formRef = ref();
const refOptions = ref<Record<string, any[]>>({});

const modelValue = computed({
  get: () => props.data,
  set: (val) => emit('update:data', val),
});

// 关联实体数据缓存
const referenceData = ref<Record<string, any[]>>({});
const referenceLoading = ref<Record<string, boolean>>({});

const rules = computed(() => {
  const r: Record<string, any[]> = {};
  props.fields.forEach(field => {
    const fieldRules: any[] = [];
    
    if (!field.nullable && !field.isPrimaryKey) {
      fieldRules.push({ required: true, message: `请输入${field.name}`, type: 'error' });
    }
    
    if (field.validationRule) {
      try {
        const rule = JSON.parse(field.validationRule);
        if (rule.min !== undefined && rule.max !== undefined) {
          fieldRules.push({
            min: rule.min,
            max: rule.max,
            message: `${field.name}长度应在 ${rule.min} 到 ${rule.max} 之间`,
            type: 'error',
          });
        }
        if (rule.pattern) {
          fieldRules.push({
            pattern: new RegExp(rule.pattern),
            message: `${field.name}格式不正确`,
            type: 'error',
          });
        }
      } catch {}
    }
    
    if (field.fieldType === 'INTEGER' || field.fieldType === 'LONG') {
      fieldRules.push({ pattern: /^-?\d+$/, message: '请输入整数', type: 'error' });
    } else if (field.fieldType === 'DOUBLE' || field.fieldType === 'DECIMAL') {
      fieldRules.push({ pattern: /^-?\d+(\.\d+)?$/, message: '请输入数字', type: 'error' });
    }
    
    if (fieldRules.length > 0) {
      r[field.code] = fieldRules;
    }
  });
  return r;
});

function getFieldSpan(field: FieldMeta): number {
  if (field.fieldType === 'TEXT' || field.fieldType === 'JSON') {
    return 12;
  }
  return 6;
}

function getInputComponent(field: FieldMeta): string {
  switch (field.fieldType) {
    case 'VARCHAR':
      return 't-input';
    case 'TEXT':
    case 'JSON':
      return 't-textarea';
    case 'INTEGER':
    case 'LONG':
    case 'DOUBLE':
    case 'DECIMAL':
      return 't-input-number';
    case 'BOOLEAN':
      return 't-switch';
    case 'DATE':
      return 't-date-picker';
    case 'DATETIME':
      return 't-date-picker';
    case 'REFERENCE':
      return 't-select';
    default:
      return 't-input';
  }
}

// 加载关联实体数据
async function loadReferenceData(entityCode: string) {
  if (!entityCode || referenceData.value[entityCode]) return;
  
  referenceLoading.value[entityCode] = true;
  try {
    const res = await dynamicDataApi.list(entityCode, { pageSize: 1000 });
    if (res.data.code === 1) {
      referenceData.value[entityCode] = res.data.data.content || res.data.data.records || [];
    }
  } catch (e) {
    console.error('加载关联实体数据失败:', e);
  } finally {
    referenceLoading.value[entityCode] = false;
  }
}

// 获取 REFERENCE 字段的下拉选项
function getReferenceOptions(field: FieldMeta): { label: string; value: any }[] {
  const entityCode = field.referenceEntityCode;
  if (!entityCode || !referenceData.value[entityCode]) return [];
  
  const displayField = field.referenceDisplayFieldCode || 'name';
  const data = referenceData.value[entityCode];
  
  return data.map(item => ({
    label: item[displayField] || item.name || `ID: ${item.id}`,
    value: item.id,
  }));
}

// 组件挂载时加载所有 REFERENCE 字段的关联数据
onMounted(() => {
  const referenceFields = props.fields.filter(f => f.fieldType === 'REFERENCE' && f.referenceEntityCode);
  referenceFields.forEach(f => loadReferenceData(f.referenceEntityCode!));
});

// 监听 fields 变化，加载新的关联数据
watch(() => props.fields, (newFields) => {
  const referenceFields = newFields.filter(f => f.fieldType === 'REFERENCE' && f.referenceEntityCode);
  referenceFields.forEach(f => loadReferenceData(f.referenceEntityCode!));
}, { deep: true });

function getInputProps(field: FieldMeta): Record<string, any> {
  const props: Record<string, any> = {};
  
  switch (field.fieldType) {
    case 'VARCHAR':
      props.maxlength = field.length || 255;
      break;
    case 'TEXT':
    case 'JSON':
      props.maxlength = 65535;
      props.autosize = { minRows: 3, maxRows: 10 };
      break;
    case 'INTEGER':
      props.decimalPlaces = 0;
      break;
    case 'DECIMAL':
      props.decimalPlaces = field.scale || 2;
      break;
    case 'DATE':
      props.mode = 'date';
      props.format = 'YYYY-MM-DD';
      props.valueType = 'YYYY-MM-DD';
      break;
    case 'DATETIME':
      props.mode = 'datetime';
      props.format = 'YYYY-MM-DD HH:mm:ss';
      props.valueType = 'YYYY-MM-DD HH:mm:ss';
      props.enableTimePicker = true;
      break;
    case 'REFERENCE':
      props.options = refOptions.value[field.code] || [];
      break;
  }
  
  return props;
}

function getPlaceholder(field: FieldMeta): string {
  if (props.readonly) return '';
  if (field.fieldType === 'REFERENCE') {
    return `请选择${field.name}`;
  }
  return `请输入${field.name}`;
}

async function loadRefOptions() {
  const refFields = props.fields.filter(f => f.fieldType === 'REFERENCE' && f.refEntityCode);
  for (const field of refFields) {
    try {
      const res = await dynamicDataApi.list(field.refEntityCode!, { page: 1, pageSize: 1000 });
      if (res.data.code === 1) {
        const records = res.data.data.content || res.data.data.records || [];
        const displayField = field.refDisplayCode || 'id';
        refOptions.value[field.code] = records.map((r: any) => ({
          label: r[displayField] !== undefined ? String(r[displayField]) : String(r.id),
          value: r.id,
        }));
      }
    } catch (e) {
      console.warn(`Failed to load ref options for ${field.code}`, e);
    }
  }
}

async function validate() {
  return formRef.value?.validate();
}

function reset() {
  formRef.value?.reset();
}

onMounted(() => {
  loadRefOptions();
});

defineExpose({ validate, reset });
</script>
