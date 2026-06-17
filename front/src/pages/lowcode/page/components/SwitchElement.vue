<template>
  <div class="render-switch">
    <span v-if="element.props.labelBefore" class="switch-label before">{{ element.props.labelBefore }}</span>
    <t-switch
      v-bind="switchProps"
    />
    <span v-if="element.props.labelAfter" class="switch-label after">{{ element.props.labelAfter }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ element: any }>();

const switchProps = computed(() => ({
  size: (props.element.props.size || 'medium') as 'small' | 'medium' | 'large',
  disabled: props.element.props.disabled ?? false,
  label: props.element.props.innerLabel ? [props.element.props.offLabel || '关', props.element.props.onLabel || '开'] as [string, string] : undefined,
  modelValue: props.element.props.checked ?? false,
}));
</script>

<style scoped>
.render-switch {
  display: flex;
  align-items: center;
  gap: 8px;
}
.switch-label { font-size: 13px; color: #374151; }
.switch-label.before { margin-right: 4px; }
.switch-label.after { margin-left: 4px; }
</style>
