<template>
  <div class="text-element">
    <span 
      :style="{
        fontSize: element.props.fontSize + 'px',
        color: element.props.color,
        textAlign: element.props.align,
      }"
    >
      {{ element.props.text || '文本内容' }}
    </span>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  element: any;
}>();
</script>

<style scoped lang="less">
.text-element {
  padding: 8px 0;
}
</style>