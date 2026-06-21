<template>
  <input
    :placeholder="element.props.placeholder || '请输入'"
    :type="element.props.type || 'text'"
    class="plain-input"
  />
</template>

<script setup lang="ts">
defineProps<{
  element: any;
}>()
</script>

<style scoped>
.plain-input {
  width: 100%;
  height: 100%;
  padding: 6px 10px;
  font-size: 14px;
  border: 1px solid #dcdcdc;
  border-radius: 4px;
  outline: none;
  box-sizing: border-box;
  background: #fff;
  color: #333;
}
.plain-input:focus {
  border-color: #4d90fe;
  box-shadow: 0 0 0 2px rgba(77,144,254,0.15);
}
</style>
