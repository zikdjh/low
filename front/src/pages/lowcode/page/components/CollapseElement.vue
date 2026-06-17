<template>
  <t-collapse v-bind="collapseProps">
    <t-collapse-panel
      v-for="(panel, i) in panels"
      :key="i"
      :header="panel.title"
      :value="String(i)"
    >
      <div class="collapse-content">{{ panel.content }}</div>
    </t-collapse-panel>
  </t-collapse>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ element: any }>();

const collapseProps = computed(() => ({
  expandMutex: props.element.props.accordion ?? true,
  borderless: props.element.props.borderless ?? false,
  defaultExpandAll: props.element.props.expandAll ?? false,
}));

const panels = computed(() => {
  const raw = props.element.props.panels || [];
  if (Array.isArray(raw) && raw.length) return raw;
  return [
    { title: '面板 1', content: '面板 1 的内容...' },
    { title: '面板 2', content: '面板 2 的内容...' },
  ];
});
</script>

<style scoped>
.collapse-content {
  color: #6b7280;
  font-size: 13px;
  padding: 4px 0;
}
</style>
