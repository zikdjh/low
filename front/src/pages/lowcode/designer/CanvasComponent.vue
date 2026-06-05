<template>
  <div
    class="canvas-component"
    :class="{ selected, 'has-children': component.children.length > 0 }"
    @click.stop="handleClick"
    @drop="handleDrop"
    @dragover.prevent
  >
    <div class="component-resize-handle top"></div>
    <div class="component-resize-handle right"></div>
    <div class="component-resize-handle bottom"></div>
    <div class="component-resize-handle left"></div>
    
    <component
      :is="renderComponent"
      v-bind="mergedProps"
      class="inner-component"
    >
      <template v-if="component.children.length > 0">
        <CanvasComponent
          v-for="child in component.children"
          :key="child.id"
          :component="child"
          :selected="selectedComponentId === child.id"
          @select="$emit('select', $event)"
          @drop="$emit('drop', $event)"
        />
      </template>
    </component>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ComponentInstance } from '../../../types/lowcode';
import { useDesignerStore } from '../../../store/modules/designer';
import ButtonElement from '../page/components/ButtonElement.vue';
import CardElement from '../page/components/CardElement.vue';
import ChartElement from '../page/components/ChartElement.vue';
import CheckboxElement from '../page/components/CheckboxElement.vue';
import DateElement from '../page/components/DateElement.vue';
import DividerElement from '../page/components/DividerElement.vue';
import FormElement from '../page/components/FormElement.vue';
import GridElement from '../page/components/GridElement.vue';
import InputElement from '../page/components/InputElement.vue';
import ListElement from '../page/components/ListElement.vue';
import RadioElement from '../page/components/RadioElement.vue';
import SelectElement from '../page/components/SelectElement.vue';
import SpaceElement from '../page/components/SpaceElement.vue';
import TableElement from '../page/components/TableElement.vue';
import TextElement from '../page/components/TextElement.vue';

const props = defineProps<{
  component: ComponentInstance;
  selected: boolean;
}>();

const emit = defineEmits<{
  (e: 'select', id: string): void;
  (e: 'drop', event: DragEvent): void;
}>();

const designerStore = useDesignerStore();
const { selectedComponentId } = designerStore;

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

function handleClick() {
  emit('select', props.component.id);
}

function handleDrop(event: DragEvent) {
  emit('drop', event);
}
</script>

<style scoped>
.canvas-component {
  position: relative;
  margin: 8px 0;
  border-radius: 4px;
  transition: all 0.2s;
}

.canvas-component.selected {
  outline: 2px solid #1890ff;
  outline-offset: 2px;
  background: rgba(24, 144, 255, 0.05);
}

.canvas-component.has-children {
  padding: 8px;
}

.component-resize-handle {
  position: absolute;
  width: 8px;
  height: 8px;
  background: #1890ff;
  border-radius: 50%;
  cursor: nwse-resize;
  opacity: 0;
  transition: opacity 0.2s;
}

.canvas-component.selected .component-resize-handle {
  opacity: 1;
}

.component-resize-handle.top {
  top: -4px;
  left: 50%;
  transform: translateX(-50%);
  cursor: ns-resize;
}

.component-resize-handle.right {
  right: -4px;
  top: 50%;
  transform: translateY(-50%);
  cursor: ew-resize;
}

.component-resize-handle.bottom {
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  cursor: ns-resize;
}

.component-resize-handle.left {
  left: -4px;
  top: 50%;
  transform: translateY(-50%);
  cursor: ew-resize;
}

.inner-component {
  width: 100%;
}
</style>