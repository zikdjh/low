<template>
  <t-alert
    :theme="element.props.theme || 'info'"
    :title="element.props.title || '提示信息'"
    :message="element.props.message || '这是一条提示消息'"
    :close="element.props.closable ?? false"
  />
</template>

<script setup lang="ts">
defineProps<{ element: any }>();
</script>
