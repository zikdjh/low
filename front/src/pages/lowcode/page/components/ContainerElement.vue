<template>
  <div class="render-container" :style="containerStyle">
    <div v-if="element.props.title" class="container-header">
      <span class="container-title">{{ element.props.title }}</span>
      <t-space v-if="element.props.showExtra" :size="8">
        <slot name="extra" />
      </t-space>
    </div>
    <div class="container-body" :style="bodyStyle">
      <slot />
      <div v-if="!element.props.content" class="container-placeholder">
        <t-icon name="layers" size="32" />
        <p>容器内容区域</p>
        <span>拖入其他组件到此区域</span>
      </div>
    </div>
    <div v-if="element.props.showFooter" class="container-footer">
      {{ element.props.footerText || '页脚' }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ element: any }>();

const containerStyle = computed(() => ({
  borderRadius: (props.element.props.borderRadius || 8) + 'px',
  background: props.element.props.bgColor || '#ffffff',
  boxShadow: props.element.props.shadow ? '0 1px 3px rgba(0,0,0,0.1)' : 'none',
}));

const bodyStyle = computed(() => ({
  padding: (props.element.props.padding || 16) + 'px',
  minHeight: '60px',
}));
</script>

<style scoped>
.render-container {
  border: 1px solid #e5e7eb;
  overflow: hidden;
}
.container-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}
.container-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}
.container-body {
  display: flex;
  flex-direction: column;
}
.container-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
  color: #9ca3af;
  gap: 4px;
}
.container-placeholder p { margin: 8px 0 0; font-size: 13px; }
.container-placeholder span { font-size: 11px; }
.container-footer {
  padding: 8px 16px;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
  font-size: 12px;
  color: #9ca3af;
  text-align: center;
}
</style>
