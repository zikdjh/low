<template>
  <div class="theme-setting">
    <div class="setting-item">
      <label>主题模式</label>
      <t-radio-group v-model="currentMode" @change="handleModeChange">
        <t-radio value="light">浅色</t-radio>
        <t-radio value="dark">深色</t-radio>
        <t-radio value="auto">跟随系统</t-radio>
      </t-radio-group>
    </div>

    <div class="setting-item">
      <label>主题色</label>
      <ColorPicker v-model="currentTheme" @update:modelValue="handleThemeChange"/>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed} from 'vue';
import {useSettingStore} from '../../store';
import {smoothThemeTransition} from '../../utils/theme';
import ColorPicker from './components/ColorPicker.vue';

const settingStore = useSettingStore();

const currentMode = computed({
  get: () => settingStore.mode,
  set: (value) => settingStore.updateConfig({mode: value})
});

const currentTheme = computed({
  get: () => settingStore.brandTheme,
  set: (value) => settingStore.updateConfig({brandTheme: value})
});

function handleModeChange(value: string) {
  // 使用平滑过渡效果切换模式
  smoothThemeTransition(() => {
    settingStore.updateConfig({mode: value});
  });
}

function handleThemeChange(value: string) {
  // 使用平滑过渡效果切换主题色
  smoothThemeTransition(() => {
    settingStore.updateConfig({brandTheme: value});
  });
}
</script>

<style scoped>
.theme-setting {
  padding: 16px;
}

.setting-item {
  margin-bottom: 16px;
}

.setting-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: var(--td-text-color-primary);
}
</style>