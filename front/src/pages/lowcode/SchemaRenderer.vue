<template>
  <div class="schema-renderer" v-if="!component">
    <div v-for="comp in componentTree" :key="comp.id">
      <div class="render-component">
        <component
          :is="getRenderComponent(comp.compKey)"
          v-bind="getMergedProps(comp.props)"
          class="inner-component"
        >
          <template v-if="comp.children.length > 0">
            <div v-for="child in comp.children" :key="child.id">
              <div class="render-component">
                <component
                  :is="getRenderComponent(child.compKey)"
                  v-bind="getMergedProps(child.props)"
                  class="inner-component"
                >
                  <template v-if="child.children.length > 0">
                    <div v-for="grandchild in child.children" :key="grandchild.id">
                      <div class="render-component">
                        <component
                          :is="getRenderComponent(grandchild.compKey)"
                          v-bind="getMergedProps(grandchild.props)"
                          class="inner-component"
                        />
                      </div>
                    </div>
                  </template>
                </component>
              </div>
            </div>
          </template>
        </component>
      </div>
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

defineProps<{
  component?: ComponentInstance;
}>();

function getRenderComponent(compKey: string) {
  return componentMap[compKey] || TextElement;
}

function getMergedProps(propsData: Record<string, any>) {
  return {
    ...propsData,
    modelValue: propsData.value,
  };
}
</script>

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