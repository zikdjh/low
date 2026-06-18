<template>
  <div class="color-picker">
    <div class="color-options">
      <div
          v-for="color in DEFAULT_COLOR_OPTIONS"
          :key="color"
          class="color-option"
          :class="{ active: modelValue === color }"
          :style="{ backgroundColor: color }"
          @click="handleColorSelect(color)"
      />
    </div>
    <div class="custom-color">
      <div
          class="color-wheel"
          :style="{ background: panelColor }"
          @click="handleCustomColor"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {DEFAULT_COLOR_OPTIONS} from '../../../config/color.ts';

const {modelValue} = defineProps<{
  modelValue: string;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: string];
}>();

const panelColor = 'conic-gradient(from 90deg at 50% 50%, #FF0000 -19.41deg, #FF0000 18.76deg, #FF8A00 59.32deg, #FFE600 99.87deg, #14FF00 141.65deg, #00A3FF 177.72deg, #0500FF 220.23deg, #AD00FF 260.13deg, #FF00C7 300.69deg, #FF0000 340.59deg, #FF0000 378.76deg)';

function handleColorSelect(color: string) {
  emit('update:modelValue', color);
}

function handleCustomColor() {
  // 这里可以集成颜色选择器库，如 @simonwep/pickr
  // 暂时使用浏览器原生颜色选择器
  const input = document.createElement('input');
  input.type = 'color';
  input.value = modelValue;
  input.onchange = () => {
    emit('update:modelValue', input.value);
  };
  input.click();
}
</script>

<style scoped>
.color-picker {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.color-options {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 8px;
  width: fit-content;
}

.color-option {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.color-option:hover,
.color-option.active {
  border-color: var(--td-brand-color);
  transform: scale(1.1);
}

.custom-color {
  display: flex;
  align-items: center;
}

.color-wheel {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid var(--td-border-level-1-color);
  transition: all 0.2s;
}

.color-wheel:hover {
  border-color: var(--td-brand-color);
  transform: scale(1.1);
}
</style>