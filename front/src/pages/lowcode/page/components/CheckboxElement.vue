<template>
  <div class="checkbox-element">
    <t-checkbox :checked="element.props.checked">
      {{ element.props.label || '选项' }}
    </t-checkbox>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  element: any;
}>();
</script>

<style scoped lang="less">
.checkbox-element {
  padding: 8px 0;
}
</style>
