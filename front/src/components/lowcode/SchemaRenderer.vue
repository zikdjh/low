<template>
  <!--
    低代码 Schema 渲染器：吃一份扁平 elements 列表，按 type 找到组件渲染。
    设计器 / 预览 / 运行时三处共用，避免 32 项 componentMap 多处复制走形。

    使用：
      <SchemaRenderer :elements="pageElements" :enable-events="true" />

    layoutJson 解析为 elements 的逻辑交给调用方（PageViewer / AppRuntime 都已有），
    本组件只关心从 elements 到 DOM 的映射，保持单一职责。
  -->
  <div class="schema-renderer">
    <div
      v-for="element in elements"
      :key="element.id"
      class="page-element"
      :style="{
        left: element.x + 'px',
        top: element.y + 'px',
        width: element.width + 'px',
        height: element.height + 'px',
      }"
    >
      <component
        :is="getElementComponent(element.type)"
        :element="element"
        :enable-events="enableEvents"
      />
    </div>
    <slot v-if="!elements || elements.length === 0" name="empty">
      <div class="schema-renderer-empty">
        <LayoutIcon size="56" />
        <h3>空白页面</h3>
        <p>该页面尚未添加任何组件</p>
      </div>
    </slot>
  </div>
</template>

<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { LayoutIcon } from 'tdesign-icons-vue-next';

defineProps<{
  elements: any[];
  /**
   * 是否启用组件内的事件（按钮 click / 表单 submit 等）。
   * 设计器 = false（只是占位），预览 / 运行时 = true。
   */
  enableEvents?: boolean;
}>();

// ===== 唯一组件注册表 =====
// 32 项，含表单 / 容器 / 布局 / 业务展示等
// 如需新增组件类型：在 ../../pages/lowcode/page/components/ 下新建 XxxElement.vue，
// 然后在这里加一行映射即可，PageViewer / AppRuntime 自动同步。
const componentMap: Record<string, any> = {
  text: () => import('../../pages/lowcode/page/components/TextElement.vue'),
  button: () => import('../../pages/lowcode/page/components/ButtonElement.vue'),
  link: () => import('../../pages/lowcode/page/components/LinkElement.vue'),
  image: () => import('../../pages/lowcode/page/components/ImageElement.vue'),
  input: () => import('../../pages/lowcode/page/components/InputElement.vue'),
  textarea: () => import('../../pages/lowcode/page/components/TextareaElement.vue'),
  inputNumber: () => import('../../pages/lowcode/page/components/InputNumberElement.vue'),
  select: () => import('../../pages/lowcode/page/components/SelectElement.vue'),
  date: () => import('../../pages/lowcode/page/components/DateElement.vue'),
  time: () => import('../../pages/lowcode/page/components/TimeElement.vue'),
  switch: () => import('../../pages/lowcode/page/components/SwitchElement.vue'),
  checkbox: () => import('../../pages/lowcode/page/components/CheckboxElement.vue'),
  radio: () => import('../../pages/lowcode/page/components/RadioElement.vue'),
  slider: () => import('../../pages/lowcode/page/components/SliderElement.vue'),
  rate: () => import('../../pages/lowcode/page/components/RateElement.vue'),
  upload: () => import('../../pages/lowcode/page/components/UploadElement.vue'),
  table: () => import('../../pages/lowcode/page/components/TableElement.vue'),
  form: () => import('../../pages/lowcode/page/components/FormElement.vue'),
  list: () => import('../../pages/lowcode/page/components/ListElement.vue'),
  chart: () => import('../../pages/lowcode/page/components/ChartElement.vue'),
  card: () => import('../../pages/lowcode/page/components/CardElement.vue'),
  tag: () => import('../../pages/lowcode/page/components/TagElement.vue'),
  progress: () => import('../../pages/lowcode/page/components/ProgressElement.vue'),
  steps: () => import('../../pages/lowcode/page/components/StepsElement.vue'),
  alert: () => import('../../pages/lowcode/page/components/AlertElement.vue'),
  divider: () => import('../../pages/lowcode/page/components/DividerElement.vue'),
  container: () => import('../../pages/lowcode/page/components/ContainerElement.vue'),
  grid: () => import('../../pages/lowcode/page/components/GridElement.vue'),
  tabs: () => import('../../pages/lowcode/page/components/TabsElement.vue'),
  collapse: () => import('../../pages/lowcode/page/components/CollapseElement.vue'),
  space: () => import('../../pages/lowcode/page/components/SpaceElement.vue'),
  breadcrumb: () => import('../../pages/lowcode/page/components/BreadcrumbElement.vue'),
};

function getElementComponent(type: string) {
  const loader = componentMap[type];
  if (loader) {
    return defineAsyncComponent(loader);
  }
  return null;
}
</script>

<style scoped lang="less">
.schema-renderer {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: inherit;
}

.page-element {
  position: absolute;
  overflow: hidden;
}

.schema-renderer-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #ccc;
  gap: 8px;
  h3 { font-size: 18px; color: #aaa; margin: 0; }
  p { font-size: 14px; color: #bbb; margin: 0 0 12px; }
}
</style>
