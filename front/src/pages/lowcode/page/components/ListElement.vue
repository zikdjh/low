<template>
  <div class="list-element">
    <t-list hover>
      <t-list-item label="列表项1" />
      <t-list-item label="列表项2" />
      <t-list-item label="列表项3" />
    </t-list>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  element: any;
}>();
</script>

<style scoped lang="less">
.list-element {
  padding: 8px 0;
}
</style>
