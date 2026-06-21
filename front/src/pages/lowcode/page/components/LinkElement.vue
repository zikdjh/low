<template>
  <div class="render-link">
    <t-link
      :href="(enableEvents && element?.events?.length) ? 'javascript:void(0)' : (element.props.href || 'javascript:;')"
      :theme="element.props.theme || 'primary'"
      :underline="element.props.underline ?? false"
      :hover="element.props.hover || 'color'"
      :disabled="element.props.disabled ?? false"
      :target="(enableEvents && element?.events?.length) ? '_self' : (element.props.target || '_self')"
      @click="handleClick"
    >
      <template v-if="element.props.icon" #prefix-icon>
        <t-icon :name="element.props.icon" />
      </template>
      {{ element.props.text || '链接文字' }}
    </t-link>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';

const props = defineProps<{
  element: any;
  enableEvents?: boolean;
}>();

const router = useRouter();

function handleClick(e: MouseEvent) {
  if (!props.enableEvents || !props.element?.events) return;
  
  const clickEvents = props.element.events.filter(
    (evt: any) => evt.trigger === 'click'
  );
  
  if (clickEvents.length > 0) {
    e.preventDefault();
    for (const evt of clickEvents) {
      executeAction(evt);
    }
  }
}

function executeAction(evt: any) {
  const { action, config } = evt;
  
  switch (action) {
    case 'navigate':
      if (config.pageId) {
        const id = config.pageId;
        if (/^\d+$/.test(String(id))) {
          router.push(`/lowcode/page/design?id=${id}`);
        } else {
          router.push(`/lowcode/page/preview/${id}`);
        }
      }
      break;
    case 'openForm':
      if (config.entityCode) {
        router.push(`/lowcode/data/${config.entityCode}?mode=create`);
      }
      break;
    case 'openDataList':
      if (config.entityCode) {
        router.push(`/lowcode/data/${config.entityCode}`);
      }
      break;
    case 'custom':
      MessagePlugin.info(`执行自定义逻辑: ${evt.label}`);
      break;
  }
}
</script>

<style scoped>
.render-link { display: inline-block; }
</style>
