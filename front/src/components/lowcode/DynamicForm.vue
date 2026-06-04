<template>
  <t-form ref="formRef" :data="modelValue" :rules="rules" label-width="120px">
    <t-row :gutter="24">
      <t-col v-for="field in fields" :key="field.code" :span="getFieldSpan(field)">
        <t-form-item :label="field.name" :name="field.code">
          <component
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
import { ref, computed } from 'vue';
import type { FieldMeta } from '../../types/lowcode';

const props = defineProps<{
  fields: FieldMeta[];
  data: Record<string, any>;
  readonly?: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:data', value: Record<string, any>): void;
}>();

const formRef = ref();

const modelValue = computed({
  get: () => props.data,
  set: (val) => emit('update:data', val),
});

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
    default:
      return 't-input';
  }
}

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
  }
  
  return props;
}

function getPlaceholder(field: FieldMeta): string {
  if (props.readonly) return '';
  return `请输入${field.name}`;
}

async function validate() {
  return formRef.value?.validate();
}

function reset() {
  formRef.value?.reset();
}

defineExpose({ validate, reset });
</script>
