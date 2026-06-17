<template>
  <t-input-number
    v-bind="inputNumberProps"
    style="width: 100%"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ element: any }>();

const inputNumberProps = computed(() => ({
  modelValue: props.element.props.value ?? 0,
  min: props.element.props.min ?? undefined,
  max: props.element.props.max ?? undefined,
  step: props.element.props.step ?? 1,
  disabled: props.element.props.disabled ?? false,
  placeholder: props.element.props.placeholder || '请输入数字',
  theme: (props.element.props.theme || 'normal') as 'normal' | 'column',
  size: (props.element.props.size || 'medium') as 'small' | 'medium' | 'large',
}));
</script>
