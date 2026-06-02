<template>
  <div class="card-element">
    <t-card 
      :title="element.props.title || ''"
      :border="element.props.border"
      :hover-shadow="element.props.hoverShadow"
    >
      <div class="card-placeholder">
        <t-icon name="layout" size="24" />
        <span>卡片内容区域</span>
      </div>
    </t-card>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  element: any;
}>();
</script>

<style scoped lang="less">
.card-element {
  .card-placeholder {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 20px;
    color: #8f959e;
  }
}
</style>
