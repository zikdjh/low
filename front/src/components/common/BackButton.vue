<template>
  <button class="back-btn" @click="handleBack">
    <ChevronLeftIcon size="18" />
    <span>{{ label }}</span>
  </button>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { ChevronLeftIcon } from 'tdesign-icons-vue-next';

const props = withDefaults(defineProps<{
  /** 目标路由路径 */
  to: string;
  /** 按钮显示文字 */
  label?: string;
}>(), {
  label: '返回',
});

const emit = defineEmits<{
  (e: 'click'): void;
}>();

const router = useRouter();

function handleBack() {
  emit('click');
  router.push(props.to);
}
</script>

<style scoped>
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px 6px 8px;
  border: none;
  background: transparent;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
  white-space: nowrap;
}
.back-btn:hover {
  background: #f3f4f6;
  color: var(--td-brand-color, #E8A317);
}
</style>
