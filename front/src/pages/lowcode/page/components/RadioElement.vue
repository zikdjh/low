<template>
  <div class="radio-element">
    <t-radio-group direction="horizontal">
      <t-radio value="1">选项1</t-radio>
      <t-radio value="2">选项2</t-radio>
    </t-radio-group>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  element: any;
}>();
</script>

<style scoped lang="less">
.radio-element {
  padding: 8px 0;
}
</style>
