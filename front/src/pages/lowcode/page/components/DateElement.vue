<template>
  <div class="date-element">
    <t-date-picker :placeholder="element.props.placeholder || '选择日期'" />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  element: any;
}>();
</script>

<style scoped lang="less">
.date-element {
  padding: 8px 0;
}
</style>
