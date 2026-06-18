<template>
  <div class="chart-element">
    <div class="chart-placeholder">
      <t-icon name="chart-bar" size="32" />
      <span>图表组件</span>
      <p>数据源：{{ element.props.dataSource || '未配置' }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  element: any;
}>();
</script>

<style scoped lang="less">
.chart-element {
  .chart-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40px;
    background: #f5f6f8;
    border-radius: 8px;
    color: #8f959e;
    
    span {
      margin-top: 12px;
      font-size: 14px;
    }
    
    p {
      margin-top: 8px;
      font-size: 12px;
    }
  }
}
</style>
