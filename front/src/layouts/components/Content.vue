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
  padding: 24px;
  overflow-y: auto;
  background: #f8fafc;
  min-height: calc(100vh - 70px);
  transition: margin-left 0.3s;
}
</style>
