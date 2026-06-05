<template>
  <div class="component-panel">
    <div class="panel-header">
      <h3>组件库</h3>
    </div>
    <div class="panel-content">
      <div v-for="(components, category) in groupedComponents" :key="category" class="category-group">
        <div class="category-title">{{ category }}</div>
        <div class="component-list">
          <div
            v-for="comp in components"
            :key="comp.compKey"
            class="component-item"
            draggable="true"
            @dragstart="handleDragStart($event, comp)"
            @click="handleClick(comp)"
          >
            <div class="component-icon-wrapper">
              <svg class="component-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <use :href="`#icon-${comp.icon}`" />
              </svg>
            </div>
            <span class="component-name">{{ comp.name }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { componentDefApi } from '../../../api/lowcode/componentDef';
import type { ComponentDef } from '../../../types/lowcode';

const emit = defineEmits<{
  (e: 'add-component', compKey: string, props: Record<string, any>): void;
}>();

const groupedComponents = ref<Record<string, ComponentDef[]>>({});

onMounted(async () => {
  const res = await componentDefApi.getComponentsGrouped();
  if (res.code === 0) {
    groupedComponents.value = res.data;
  }
});

function handleDragStart(event: DragEvent, comp: ComponentDef) {
  if (event.dataTransfer) {
    event.dataTransfer.setData('component', JSON.stringify({
      compKey: comp.compKey,
      props: JSON.parse(comp.defaultPropsJson || '{}'),
    }));
    event.dataTransfer.effectAllowed = 'copy';
  }
}

function handleClick(comp: ComponentDef) {
  emit('add-component', comp.compKey, JSON.parse(comp.defaultPropsJson || '{}'));
}
</script>

<style scoped lang="less">
.component-panel {
  width: 260px;
  height: 100%;
  background: #ffffff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
}

.panel-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.category-group {
  margin-bottom: 24px;
}

.category-title {
  font-size: 11px;
  color: #64748b;
  margin-bottom: 10px;
  padding-left: 6px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.component-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.component-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 14px 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  cursor: grab;
  transition: all 0.25s ease;
  
  &:hover {
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
    border-color: #60a5fa;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(96, 165, 250, 0.2);
  }
  
  &:active {
    cursor: grabbing;
    transform: translateY(0);
  }
}

.component-icon-wrapper {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #ffffff 0%, #f1f5f9 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  border: 1px solid #e2e8f0;
}

.component-icon {
  width: 20px;
  height: 20px;
  color: #475569;
}

.component-name {
  font-size: 12px;
  color: #334155;
  font-weight: 500;
  text-align: center;
}
</style>
