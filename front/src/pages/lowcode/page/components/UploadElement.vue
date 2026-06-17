<template>
  <t-upload
    v-bind="uploadProps"
    style="width: 100%"
  >
    <div class="upload-trigger" :class="{ disabled: element.props.disabled }">
      <t-icon name="upload" size="24" />
      <p>{{ element.props.hint || '点击或拖拽上传文件' }}</p>
      <span v-if="element.props.maxSize" class="upload-limit">
        单文件不超过 {{ element.props.maxSize }}MB
      </span>
    </div>
  </t-upload>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ element: any }>();

const uploadProps = computed(() => ({
  disabled: props.element.props.disabled ?? false,
  multiple: props.element.props.multiple ?? false,
  max: props.element.props.max ?? 5,
  theme: 'custom' as const,
  draggable: props.element.props.draggable ?? true,
}));
</script>

<style scoped>
.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 20px;
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: #9ca3af;
  background: #f9fafb;
}
.upload-trigger:hover:not(.disabled) {
  border-color: var(--td-brand-color, #E8A317);
  color: var(--td-brand-color, #E8A317);
  background: rgba(232, 163, 23, 0.04);
}
.upload-trigger p { margin: 8px 0; font-size: 13px; }
.upload-trigger .upload-limit { font-size: 11px; }
.upload-trigger.disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
</style>
