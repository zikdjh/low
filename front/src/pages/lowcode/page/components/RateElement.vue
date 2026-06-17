<template>
  <div class="render-rate">
    <div v-if="element.props.showLabel !== false" class="rate-label">{{ element.props.label || '评分' }}</div>
    <t-rate
      :value="element.props.value || 3"
      :count="element.props.count || 5"
      :size="element.props.size || 20"
      :allow-half="element.props.allowHalf ?? false"
      :readonly="element.props.readonly ?? false"
      :color="element.props.color || '#f5a623'"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{ element: any }>();
</script>

<style scoped>
.render-rate { width: 100%; }
.rate-label { font-size: 13px; color: #374151; margin-bottom: 6px; font-weight: 500; }
</style>
