<template>
  <div class="render-progress">
    <div class="progress-info" v-if="element.props.showLabel !== false">
      <span class="progress-label">{{ element.props.label || '进度' }}</span>
      <span v-if="element.props.showPercent !== false" class="progress-percent">{{ element.props.percent || 60 }}%</span>
    </div>
    <t-progress
      :percentage="element.props.percent || 60"
      :stroke-width="element.props.strokeWidth || 8"
      :theme="element.props.theme && element.props.theme !== 'default' ? (element.props.theme as any) : undefined"
      :color="element.props.color || undefined"
      :track-color="element.props.trackColor || undefined"
      :label="false"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{ element: any }>();
</script>

<style scoped>
.render-progress { width: 100%; }
.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}
.progress-label { color: #374151; font-weight: 500; }
.progress-percent { color: #6b7280; }
</style>
