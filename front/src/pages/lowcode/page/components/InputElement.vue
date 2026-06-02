<template>
  <div class="input-element">
    <t-input 
      :placeholder="element.props.placeholder || '请输入'" 
      :status="element.props.required ? 'error' : ''"
    />
    <span v-if="element.props.required" class="required-hint">* 必填</span>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  element: any;
}>();
</script>

<style scoped lang="less">
.input-element {
  .required-hint {
    font-size: 12px;
    color: #f56c6c;
    margin-left: 8px;
  }
}
</style>
