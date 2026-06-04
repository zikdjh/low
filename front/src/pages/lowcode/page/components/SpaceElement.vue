<template>
  <div class="space-element">
    <t-space :direction="element.props.direction || 'horizontal'" :size="element.props.size || 16">
      <div class="space-box">块1</div>
      <div class="space-box">块2</div>
      <div class="space-box">块3</div>
    </t-space>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  element: any;
}>();
</script>

<style scoped lang="less">
.space-element {
  padding: 8px 0;
  
  .space-box {
    padding: 8px 16px;
    background: #f5f6f8;
    border-radius: 4px;
    color: #646a73;
    font-size: 12px;
  }
}
</style>
