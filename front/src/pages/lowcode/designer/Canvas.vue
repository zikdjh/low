<template>
  <div class="canvas-container">
    <div class="canvas-header">
      <div class="header-left">
        <t-button
          variant="text"
          :disabled="!canUndo"
          @click="handleUndo"
          title="撤销"
          class="toolbar-btn"
        >
          <template #icon><RollbackIcon size="18" /></template>
        </t-button>
        <t-button
          variant="text"
          :disabled="!canRedo"
          @click="handleRedo"
          title="重做"
          class="toolbar-btn"
        >
          <template #icon><ForwardIcon size="18" /></template>
        </t-button>
        <div class="divider"></div>
        <t-button
          v-if="selectedComponentId"
          variant="text"
          theme="danger"
          @click="handleDelete"
          title="删除"
          class="toolbar-btn"
        >
          <template #icon><DeleteIcon size="18" /></template>
        </t-button>
      </div>
      <div class="header-right">
        <t-button variant="outline" @click="handleSave">
          <template #icon><SaveIcon size="16" /></template>
          保存
        </t-button>
        <t-button theme="primary" @click="handlePreview">
          <template #icon><KeyIcon size="16" /></template>
          预览
        </t-button>
      </div>
    </div>
    <div
      class="canvas-area"
      @drop="handleDrop"
      @dragover.prevent
      @click="handleCanvasClick"
    >
      <div class="canvas">
        <div class="canvas-wrapper">
          <CanvasComponent
            v-for="comp in componentTree"
            :key="comp.id"
            :component="comp"
            :selected="selectedComponentId === comp.id"
            @select="handleSelect"
            @drop="(e) => handleComponentDrop(e, comp.id)"
          />
          <div v-if="componentTree.length === 0" class="empty-tip">
            <LayoutIcon size="64" />
            <p>拖拽组件到这里开始设计</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useDesignerStore } from '../../../store/modules/designer';
import CanvasComponent from './CanvasComponent.vue';
import { pageSchemaApi } from '../../../api/lowcode/pageSchema';
import { MessagePlugin } from 'tdesign-vue-next';
import { RollbackIcon, ForwardIcon, DeleteIcon, SaveIcon, KeyIcon, LayoutIcon } from 'tdesign-icons-vue-next';

import { storeToRefs } from 'pinia';

const designerStore = useDesignerStore();
const { componentTree, selectedComponentId, pageSchema } = storeToRefs(designerStore);
const { canUndo, canRedo, addComponent, deleteComponent, selectComponent, undo, redo, savePage } = designerStore;

function handleDrop(event: DragEvent) {
  if (event.dataTransfer) {
    const data = event.dataTransfer.getData('component');
    if (data) {
      const { compKey, props } = JSON.parse(data);
      addComponent(null, compKey, props);
    }
  }
}

function handleComponentDrop(event: DragEvent, parentId: string) {
  if (event.dataTransfer) {
    const data = event.dataTransfer.getData('component');
    if (data) {
      const { compKey, props } = JSON.parse(data);
      addComponent(parentId, compKey, props);
    }
  }
}

function handleSelect(id: string) {
  selectComponent(id);
}

function handleCanvasClick(event: MouseEvent) {
  if ((event.target as HTMLElement).classList.contains('canvas-area')) {
    selectComponent(null);
  }
}

function handleUndo() {
  undo();
}

function handleRedo() {
  redo();
}

function handleDelete() {
  if (selectedComponentId.value) {
    deleteComponent(selectedComponentId.value);
  }
}

async function handleSave() {
  const name = (pageSchema.value?.name || '未命名页面').trim() || '未命名页面';
  const code = (pageSchema.value?.pageCode || name.toLowerCase().replace(/\s+/g, '-')).trim();
  
  const saved = savePage(name, code);
  
  try {
    if (saved.id) {
      await pageSchemaApi.update(saved.id, saved);
    } else {
      await pageSchemaApi.create(saved);
    }
    MessagePlugin.success('保存成功');
  } catch (error) {
    MessagePlugin.error('保存失败');
  }
}

function handlePreview() {
  const layoutJson = JSON.stringify(componentTree.value);
  sessionStorage.setItem('previewLayout', layoutJson);
  window.open('/lowcode/preview', '_blank');
}
</script>

<style scoped lang="less">
.canvas-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
}

.canvas-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 4px;
}

.toolbar-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  
  &:hover:not(:disabled) {
    background: #f1f5f9;
  }
}

.divider {
  width: 1px;
  height: 24px;
  background: #e2e8f0;
  margin: 0 8px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.canvas-area {
  flex: 1;
  padding: 24px;
  overflow: auto;
}

.canvas {
  max-width: 1200px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.canvas-wrapper {
  min-height: 600px;
  padding: 32px;
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
  color: #94a3b8;
}

.empty-tip :deep(.t-icon) {
  margin-bottom: 16px;
  color: #cbd5e1;
}

.empty-tip p {
  margin: 0;
  font-size: 14px;
}
</style>
