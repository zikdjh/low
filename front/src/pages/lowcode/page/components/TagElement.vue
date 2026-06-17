<template>
  <t-space :size="element.props.gap || 8">
    <t-tag
      v-for="(tag, i) in tags"
      :key="i"
      :theme="tag.theme || element.props.theme || 'primary'"
      :variant="tag.variant || element.props.variant || 'light'"
      :size="element.props.size || 'medium'"
      :closable="element.props.closable ?? false"
      :max-width="element.props.maxWidth"
    >
      {{ typeof tag === 'string' ? tag : tag.label }}
    </t-tag>
  </t-space>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ element: any }>();

const tags = computed(() => {
  const raw = props.element.props.tags || [];
  if (Array.isArray(raw) && raw.length) return raw;
  return ['默认标签', '成功', '警告', '危险', '信息'];
});
</script>
