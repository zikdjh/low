<template>
  <div class="divider-element">
    <t-divider :dashed="element.props.dashed">
      {{ element.props.content }}
    </t-divider>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  element: any;
}>();
</script>

<style scoped lang="less">
.divider-element {
  padding: 8px 0;
}
</style>
