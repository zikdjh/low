<template>
  <t-time-picker
    v-bind="timePickerProps"
    style="width: 100%"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ element: any }>();

const timePickerProps = computed(() => ({
  placeholder: props.element.props.placeholder || '选择时间',
  disabled: props.element.props.disabled ?? false,
  clearable: props.element.props.clearable ?? true,
  format: props.element.props.format || 'HH:mm:ss',
  size: (props.element.props.size || 'medium') as 'small' | 'medium' | 'large',
}));
</script>
