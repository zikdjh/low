<template>
  <div class="form-element">
    <t-form :label-width="element.props.labelWidth || 100">
      <t-form-item label="字段1">
        <t-input placeholder="请输入" />
      </t-form-item>
      <t-form-item label="字段2">
        <t-input placeholder="请输入" />
      </t-form-item>
      <t-form-item>
        <t-button theme="primary" block>提交</t-button>
      </t-form-item>
    </t-form>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  element: any;
}>();
</script>

<style scoped lang="less">
.form-element {
  padding: 8px 0;
}
</style>
