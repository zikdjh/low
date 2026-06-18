<template>
  <t-steps
    :current="element.props.current ?? 0"
    :layout="element.props.layout || 'horizontal'"
    :theme="element.props.theme || 'default'"
    :readonly="element.props.readonly ?? false"
  >
    <t-step-item
      v-for="(step, i) in steps"
      :key="i"
      :title="step.title"
      :content="step.content || ''"
      :status="i < (element.props.current ?? 0) ? 'finish' : i === (element.props.current ?? 0) ? 'process' : 'default'"
    />
  </t-steps>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ element: any }>();

const steps = computed(() => {
  const raw = props.element.props.steps || [];
  if (Array.isArray(raw) && raw.length) return raw;
  return [
    { title: '步骤一', content: '描述信息' },
    { title: '步骤二', content: '描述信息' },
    { title: '步骤三', content: '描述信息' },
  ];
});
</script>
