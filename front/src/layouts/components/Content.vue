<template>
  <main class="layout-content">
    <router-view v-slot="{ Component, route }">
      <transition name="page-fade">
        <keep-alive>
          <component :is="Component" :key="route.fullPath" />
        </keep-alive>
      </transition>
    </router-view>
  </main>
</template>

<script setup lang="ts">
</script>

<style scoped lang="less">
.layout-content {
  flex: 1;
  overflow-y: auto;
  background: #f7f8fa;
  transition: margin-left 0.3s;

  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb {
    background: #d0d5dd;
    border-radius: 10px;
    &:hover { background: #b0b5bd; }
  }
}

/* 页面过渡动画 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
