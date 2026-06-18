<template>
  <main class="layout-content">
    <router-view />
  </main>
</template>

<script setup lang="ts">
</script>

<style scoped lang="less">
.layout-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f5f5f5;
  min-height: calc(100vh - 64px);
}
</style>
