<template>
  <div class="render-image" :style="wrapperStyle">
    <img
      v-if="element.props.src"
      :src="element.props.src"
      :alt="element.props.alt || '图片'"
      :style="imgStyle"
    />
    <div v-else class="image-placeholder">
      <t-icon name="image" size="40" />
      <p>{{ element.props.placeholder || '图片占位' }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ element: any }>();

const wrapperStyle = computed(() => ({
  textAlign: props.element.props.align || 'center',
}));

const imgStyle = computed(() => ({
  maxWidth: '100%',
  borderRadius: (props.element.props.radius || 8) + 'px',
  objectFit: (props.element.props.fit || 'cover') as any,
  width: props.element.props.width || 'auto',
  height: props.element.props.height || 'auto',
}));
</script>

<style scoped>
.render-image { width: 100%; overflow: hidden; }
.image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: #f3f4f6;
  border-radius: 8px;
  border: 2px dashed #d1d5db;
  color: #9ca3af;
}
.image-placeholder p { margin: 12px 0 0; font-size: 13px; }
</style>
