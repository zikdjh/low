<template>
  <t-textarea
    v-bind="textareaProps"
    style="width: 100%"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ element: any }>();

const textareaProps = computed(() => ({
  modelValue: props.element.props.text || '',
  placeholder: props.element.props.placeholder || '请输入多行文本',
  maxlength: props.element.props.maxlength || undefined,
  disabled: props.element.props.disabled ?? false,
  autosize: props.element.props.autosize !== false
    ? { minRows: props.element.props.minRows || 3, maxRows: props.element.props.maxRows || 6 }
    : false,
}));
</script>
