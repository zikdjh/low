<template>
  <router-view/>
</template>

<script setup lang="ts">
import {useSettingStore} from './store';

const settingStore = useSettingStore();
settingStore.changeBrandTheme(settingStore.brandTheme);
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: Inter, system-ui, Avenir, Helvetica, Arial, sans-serif;
  background-color: var(--td-bg-color-container);
  color: var(--td-text-color-primary);
}

</style>