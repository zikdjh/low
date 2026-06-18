<template>
  <div class="select-element">
    <t-select :placeholder="element.props.placeholder || '请选择'">
      <t-option value="1" label="选项1" />
      <t-option value="2" label="选项2" />
      <t-option value="3" label="选项3" />
    </t-select>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  element: any;
}>();
</script>

<style scoped lang="less">
.select-element {
  padding: 8px 0;
}
</style>
