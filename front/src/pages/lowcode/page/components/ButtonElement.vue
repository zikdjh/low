<template>
  <div class="button-element">
    <t-button :theme="element.props.theme || 'primary'">
      {{ element.props.text || '按钮' }}
    </t-button>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  element: any;
}>();
</script>

<style scoped lang="less">
.button-element {
  padding: 8px 0;
}
</style>
