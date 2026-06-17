<template>
  <t-tabs v-bind="tabProps">
    <t-tab-panel
      v-for="(tab, i) in tabs"
      :key="i"
      :value="String(i)"
      :label="tab"
    >
      <div class="tab-content-placeholder">
        <t-icon name="layers" size="18" />
        <span>{{ tab }} 的内容区域</span>
      </div>
    </t-tab-panel>
  </t-tabs>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ element: any }>();

const tabProps = computed(() => ({
  size: (props.element.props.size || 'medium') as 'small' | 'medium' | 'large',
  theme: (props.element.props.theme || 'normal') as 'normal' | 'card',
  placement: (props.element.props.placement || 'top') as 'top' | 'bottom' | 'left' | 'right',
}));

const tabs = computed(() => {
  const raw = props.element.props.tabs || '标签1,标签2,标签3';
  return typeof raw === 'string' ? raw.split(',') : raw;
});
</script>

<style scoped>
.tab-content-placeholder {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 24px;
  color: #9ca3af;
  font-size: 13px;
}
</style>
