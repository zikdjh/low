<template>
  <div class="grid-element">
    <t-row :gutter="element.props.gutter || 16">
      <t-col 
        v-for="i in (element.props.columns || 2)" 
        :key="i" 
        :span="24 / (element.props.columns || 2)"
      >
        <div class="grid-item">
          <t-icon name="grid-3x3" size="20" />
          <span>列 {{ i }}</span>
        </div>
      </t-col>
    </t-row>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  element: any;
}>();
</script>

<style scoped lang="less">
.grid-element {
  .grid-item {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    height: 100px;
    background: #f5f6f8;
    border-radius: 8px;
    color: #8f959e;
  }
}
</style>
