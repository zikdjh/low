<template>
  <div class="layout-container">
    <Header />
    <div class="layout-body">
      <Aside />
      <Content />
    </div>
  </div>
</template>

<script setup lang="ts">
import Header from './components/Header.vue';
import Aside from './components/Aside.vue';
import Content from './components/Content.vue';
</script>

<style scoped lang="less">
.layout-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f7f8fa;
}
.layout-body {
  display: flex;
  flex: 1;
  min-height: 0;
}
</style>
