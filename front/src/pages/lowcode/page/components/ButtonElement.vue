<template>
  <div class="button-element">
    <t-button 
      :theme="element.props.theme || 'primary'"
      :size="element.props.size || 'medium'"
      :variant="element.props.variant || 'base'"
      @click="handleClick"
    >
      {{ element.props.text || '按钮' }}
    </t-button>
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

function handleClick() {
  if (!props.enableEvents || !props.element?.events) return;
  
  const clickEvents = props.element.events.filter(
    (e: any) => e.trigger === 'click'
  );
  
  for (const evt of clickEvents) {
    executeAction(evt);
  }
}

function executeAction(evt: any) {
  const { action, config } = evt;
  
  switch (action) {
    case 'navigate':
      if (config.pageId) {
        // 判断pageId是数字还是pageCode
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

<style scoped lang="less">
.button-element {
  padding: 8px 0;
}
</style>
