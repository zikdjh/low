<template>
  <div class="designer-layout">
    <ComponentPanel @add-component="handleAddComponent" />
    <Canvas />
    <PropertyEditor />
  </div>
</template>

<script setup lang="ts">
import ComponentPanel from './ComponentPanel.vue';
import Canvas from './Canvas.vue';
import PropertyEditor from './PropertyEditor.vue';
import { useDesignerStore } from '../../../store/modules/designer';

const designerStore = useDesignerStore();
const { addComponent } = designerStore;

function handleAddComponent(compKey: string, props: Record<string, any>) {
  addComponent(null, compKey, props);
}
</script>

<style scoped>
.designer-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}
</style>