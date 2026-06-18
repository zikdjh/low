<template>
  <div class="render-link">
    <t-link
      :href="element.props.href || 'javascript:;'"
      :theme="element.props.theme || 'primary'"
      :underline="element.props.underline ?? false"
      :hover="element.props.hover || 'color'"
      :disabled="element.props.disabled ?? false"
      :target="element.props.target || '_self'"
    >
      <template v-if="element.props.icon" #prefix-icon>
        <t-icon :name="element.props.icon" />
      </template>
      {{ element.props.text || '链接文字' }}
    </t-link>
  </div>
</template>

<script setup lang="ts">
defineProps<{ element: any }>();
</script>

<style scoped>
.render-link { display: inline-block; }
</style>
