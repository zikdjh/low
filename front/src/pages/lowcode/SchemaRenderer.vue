<template>
  <div class="schema-renderer">
    <div v-for="comp in componentTree" :key="comp.id">
      <RenderComponent :component="comp" />
    </div>
    <div v-if="componentTree.length === 0" class="empty-tip">
      <p>暂无内容</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import type { ComponentInstance } from '../../types/lowcode';
import ButtonElement from './page/components/ButtonElement.vue';
import CardElement from './page/components/CardElement.vue';
import ChartElement from './page/components/ChartElement.vue';
import CheckboxElement from './page/components/CheckboxElement.vue';
import DateElement from './page/components/DateElement.vue';
import DividerElement from './page/components/DividerElement.vue';
import FormElement from './page/components/FormElement.vue';
import GridElement from './page/components/GridElement.vue';
import InputElement from './page/components/InputElement.vue';
import ListElement from './page/components/ListElement.vue';
import RadioElement from './page/components/RadioElement.vue';
import SelectElement from './page/components/SelectElement.vue';
import SpaceElement from './page/components/SpaceElement.vue';
import TableElement from './page/components/TableElement.vue';
import TextElement from './page/components/TextElement.vue';

const componentTree = ref<ComponentInstance[]>([]);

const componentMap: Record<string, any> = {
  button: ButtonElement,
  card: CardElement,
  chart: ChartElement,
  checkbox: CheckboxElement,
  date: DateElement,
  divider: DividerElement,
  form: FormElement,
  grid: GridElement,
  input: InputElement,
  list: ListElement,
  radio: RadioElement,
  select: SelectElement,
  space: SpaceElement,
  table: TableElement,
  text: TextElement,
};

onMounted(() => {
  const layoutJson = sessionStorage.getItem('previewLayout');
  if (layoutJson) {
    try {
      componentTree.value = JSON.parse(layoutJson);
    } catch (error) {
      console.error('Failed to parse layout JSON:', error);
    }
  }
});
</script>

<script setup lang="ts" name="RenderComponent">
import { computed } from 'vue';
import type { ComponentInstance } from '../../types/lowcode';

const props = defineProps<{
  component: ComponentInstance;
}>();

const componentMap: Record<string, any> = {
  button: ButtonElement,
  card: CardElement,
  chart: ChartElement,
  checkbox: CheckboxElement,
  date: DateElement,
  divider: DividerElement,
  form: FormElement,
  grid: GridElement,
  input: InputElement,
  list: ListElement,
  radio: RadioElement,
  select: SelectElement,
  space: SpaceElement,
  table: TableElement,
  text: TextElement,
};

const renderComponent = computed(() => {
  return componentMap[props.component.compKey] || TextElement;
});

const mergedProps = computed(() => {
  return {
    ...props.component.props,
    modelValue: props.component.props.value,
  };
});
</script>

<template>
  <div class="render-component">
    <component
      :is="renderComponent"
      v-bind="mergedProps"
      class="inner-component"
    >
      <template v-if="component.children.length > 0">
        <RenderComponent
          v-for="child in component.children"
          :key="child.id"
          :component="child"
        />
      </template>
    </component>
  </div>
</template>

<style scoped>
.schema-renderer {
  min-height: 100vh;
  padding: 24px;
  background: #f5f5f5;
}

.empty-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #999;
}

.empty-tip p {
  margin: 0;
}

.render-component {
  margin: 8px 0;
}

.inner-component {
  width: 100%;
}
</style>