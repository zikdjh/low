<template>
  <div class="render-slider">
    <div v-if="element.props.showLabel !== false" class="slider-header">
      <span class="slider-label">{{ element.props.label || '滑块' }}</span>
      <span class="slider-value">{{ element.props.value || 50 }}</span>
    </div>
    <t-slider
      :value="element.props.value || 50"
      :min="element.props.min ?? 0"
      :max="element.props.max ?? 100"
      :step="element.props.step ?? 1"
      :disabled="element.props.disabled ?? false"
      :show-tooltip="element.props.showTooltip ?? true"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{ element: any }>();
</script>

<style scoped>
.render-slider { width: 100%; }
.slider-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}
.slider-label { font-size: 13px; color: #374151; font-weight: 500; }
.slider-value { font-size: 13px; color: var(--td-brand-color, #E8A317); font-weight: 600; }
</style>
