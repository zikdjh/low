<template>
  <t-breadcrumb :max-items="element.props.maxItems ?? 0">
    <t-breadcrumb-item
      v-for="(item, i) in items"
      :key="i"
      :to="i < items.length - 1 ? item.path : undefined"
    >
      {{ item.label }}
    </t-breadcrumb-item>
  </t-breadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ element: any }>();

const items = computed(() => {
  const raw = props.element.props.items || [];
  if (Array.isArray(raw) && raw.length) return raw;
  return [
    { label: '首页', path: '/' },
    { label: '列表页', path: '/list' },
    { label: '详情页' },
  ];
});
</script>
