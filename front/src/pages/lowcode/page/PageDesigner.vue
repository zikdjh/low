<template>
  <div class="page-designer">
    <!-- 顶部工具栏 -->
    <div class="designer-header">
      <div class="header-left">
        <t-button variant="text" @click="goBack">
          <template #icon><t-icon name="chevron-left" /></template>
        </t-button>
        <input 
          v-model="pageName" 
          class="page-name-input"
          placeholder="请输入页面名称"
        />
      </div>
      <div class="header-right">
        <t-button variant="outline" @click="handleUndo" :disabled="!canUndo">
          <template #icon><t-icon name="undo" /></template>
          撤销
        </t-button>
        <t-button variant="outline" @click="handleRedo" :disabled="!canRedo">
          <template #icon><t-icon name="redo" /></template>
          重做
        </t-button>
        <t-button variant="outline" @click="handlePreview">
          <template #icon><t-icon name="external-link" /></template>
          预览
        </t-button>
        <t-button theme="primary" @click="handleSave">
          <template #icon><t-icon name="save" /></template>
          保存
        </t-button>
      </div>
    </div>

    <div class="designer-body">
      <!-- 左侧组件面板 -->
      <div class="component-panel">
        <div class="panel-header">
          <t-icon name="component" size="16" />
          <span>组件库</span>
        </div>
        <div class="panel-content">
          <div class="component-group">
            <div class="group-title">基础组件</div>
            <div 
              v-for="comp in basicComponents" 
              :key="comp.type"
              class="component-item"
              draggable="true"
              @dragstart="onDragStart($event, comp)"
            >
              <div class="component-icon">
                <t-icon :name="comp.icon" size="20" />
              </div>
              <span>{{ comp.label }}</span>
            </div>
          </div>
          <div class="component-group">
            <div class="group-title">数据组件</div>
            <div 
              v-for="comp in dataComponents" 
              :key="comp.type"
              class="component-item"
              draggable="true"
              @dragstart="onDragStart($event, comp)"
            >
              <div class="component-icon">
                <t-icon :name="comp.icon" size="20" />
              </div>
              <span>{{ comp.label }}</span>
            </div>
          </div>
          <div class="component-group">
            <div class="group-title">布局组件</div>
            <div 
              v-for="comp in layoutComponents" 
              :key="comp.type"
              class="component-item"
              draggable="true"
              @dragstart="onDragStart($event, comp)"
            >
              <div class="component-icon">
                <t-icon :name="comp.icon" size="20" />
              </div>
              <span>{{ comp.label }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间画布区域 -->
      <div 
        class="canvas-area"
        @dragover.prevent="onDragOver"
        @drop="onDrop"
      >
        <div class="canvas-container">
          <div 
            v-for="(element, index) in pageElements" 
            :key="element.id"
            class="canvas-element"
            :class="{ selected: selectedElementId === element.id }"
            @click="selectElement(element)"
          >
            <component 
              :is="getElementComponent(element.type)" 
              :element="element"
            />
            <div class="element-actions" v-if="selectedElementId === element.id">
              <t-button size="small" variant="text" @click.stop="handleCopy(index)">
                <t-icon name="copy" size="14" />
              </t-button>
              <t-button size="small" variant="text" @click.stop="handleMoveUp(index)" :disabled="index === 0">
                <t-icon name="chevron-up" size="14" />
              </t-button>
              <t-button size="small" variant="text" @click.stop="handleMoveDown(index)" :disabled="index === pageElements.length - 1">
                <t-icon name="chevron-down" size="14" />
              </t-button>
              <t-button size="small" variant="text" theme="danger" @click.stop="handleDelete(index)">
                <t-icon name="delete" size="14" />
              </t-button>
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-if="pageElements.length === 0" class="empty-canvas">
            <t-icon name="layout" size="48" />
            <p>从左侧拖拽组件到这里</p>
            <p class="empty-hint">开始构建您的页面</p>
          </div>
        </div>
      </div>

      <!-- 右侧属性面板 -->
      <div class="property-panel">
        <div class="panel-header">
          <t-icon name="settings" size="16" />
          <span>属性配置</span>
        </div>
        <div class="panel-content" v-if="selectedElement">
          <t-form label-width="80">
            <t-form-item label="组件名称">
              <t-input v-model="selectedElement.name" />
            </t-form-item>
            <t-form-item label="组件类型">
              <t-tag variant="light">{{ getComponentLabel(selectedElement.type) }}</t-tag>
            </t-form-item>
            <t-divider />
            <template v-if="selectedElement.type === 'text'">
              <t-form-item label="文本内容">
                <t-input v-model="selectedElement.props.text" />
              </t-form-item>
              <t-form-item label="字体大小">
                <t-input v-model="selectedElement.props.fontSize" type="number" />
              </t-form-item>
              <t-form-item label="字体颜色">
                <t-input v-model="selectedElement.props.color" />
              </t-form-item>
            </template>
            <template v-else-if="selectedElement.type === 'input'">
              <t-form-item label="占位文本">
                <t-input v-model="selectedElement.props.placeholder" />
              </t-form-item>
              <t-form-item label="是否必填">
                <t-switch v-model="selectedElement.props.required" />
              </t-form-item>
            </template>
            <template v-else-if="selectedElement.type === 'button'">
              <t-form-item label="按钮文本">
                <t-input v-model="selectedElement.props.text" />
              </t-form-item>
              <t-form-item label="按钮主题">
                <t-select v-model="selectedElement.props.theme">
                  <t-option value="primary" label="主色" />
                  <t-option value="default" label="默认" />
                  <t-option value="success" label="成功" />
                  <t-option value="warning" label="警告" />
                  <t-option value="danger" label="危险" />
                </t-select>
              </t-form-item>
            </template>
            <template v-else-if="selectedElement.type === 'table'">
              <t-form-item label="数据源">
                <t-select v-model="selectedElement.props.dataSource">
                  <t-option value="" label="请选择数据源" />
                  <t-option value="entity1" label="实体1" />
                  <t-option value="entity2" label="实体2" />
                </t-select>
              </t-form-item>
              <t-form-item label="显示边框">
                <t-switch v-model="selectedElement.props.border" />
              </t-form-item>
              <t-form-item label="显示序号">
                <t-switch v-model="selectedElement.props.showIndex" />
              </t-form-item>
            </template>
            <template v-else-if="selectedElement.type === 'card'">
              <t-form-item label="卡片标题">
                <t-input v-model="selectedElement.props.title" />
              </t-form-item>
              <t-form-item label="显示边框">
                <t-switch v-model="selectedElement.props.border" />
              </t-form-item>
              <t-form-item label="悬停阴影">
                <t-switch v-model="selectedElement.props.hoverShadow" />
              </t-form-item>
            </template>
            <template v-else-if="selectedElement.type === 'grid'">
              <t-form-item label="列数">
                <t-select v-model="selectedElement.props.columns">
                  <t-option :value="1" label="1列" />
                  <t-option :value="2" label="2列" />
                  <t-option :value="3" label="3列" />
                  <t-option :value="4" label="4列" />
                </t-select>
              </t-form-item>
              <t-form-item label="间距">
                <t-input v-model="selectedElement.props.gutter" type="number" />
              </t-form-item>
            </template>
          </t-form>
        </div>
        <div class="panel-content empty-property" v-else>
          <t-icon name="mouse-pointer" size="32" />
          <p>选择组件查看属性</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, markRaw } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';

const router = useRouter();

// 页面名称
const pageName = ref('未命名页面');

// 拖拽状态
const draggingComponent = ref<any>(null);

// 页面元素列表
interface PageElement {
  id: string;
  type: string;
  name: string;
  props: Record<string, any>;
}

const pageElements = ref<PageElement[]>([
  {
    id: '1',
    type: 'card',
    name: '标题卡片',
    props: {
      title: '欢迎使用低代码平台',
      border: true,
      hoverShadow: true,
    },
  },
  {
    id: '2',
    type: 'text',
    name: '描述文本',
    props: {
      text: '这是一个强大的低代码开发平台，支持可视化页面设计。',
      fontSize: 16,
      color: '#646a73',
    },
  },
]);

// 选中的元素
const selectedElementId = ref<string | null>(null);
const selectedElement = computed(() => {
  return pageElements.value.find(el => el.id === selectedElementId.value) || null;
});

// 历史记录（用于撤销/重做）
const history = ref<PageElement[][]>([JSON.parse(JSON.stringify(pageElements.value))]);
const historyIndex = ref(0);

const canUndo = computed(() => historyIndex.value > 0);
const canRedo = computed(() => historyIndex.value < history.value.length - 1);

// 组件定义
const basicComponents = [
  { type: 'text', label: '文本', icon: 'font' },
  { type: 'input', label: '输入框', icon: 'edit' },
  { type: 'button', label: '按钮', icon: 'button' },
  { type: 'select', label: '下拉框', icon: 'chevron-down' },
  { type: 'date', label: '日期选择', icon: 'calendar' },
  { type: 'checkbox', label: '复选框', icon: 'check-square' },
  { type: 'radio', label: '单选框', icon: 'circle' },
];

const dataComponents = [
  { type: 'table', label: '数据表格', icon: 'table' },
  { type: 'form', label: '表单', icon: 'form' },
  { type: 'list', label: '列表', icon: 'list' },
  { type: 'chart', label: '图表', icon: 'chart-bar' },
];

const layoutComponents = [
  { type: 'card', label: '卡片', icon: 'layout' },
  { type: 'grid', label: '栅格', icon: 'grid-3x3' },
  { type: 'divider', label: '分割线', icon: 'minus' },
  { type: 'space', label: '间距', icon: 'spacing-horizontal' },
];

// 获取组件标签
function getComponentLabel(type: string): string {
  const allComponents = [...basicComponents, ...dataComponents, ...layoutComponents];
  const comp = allComponents.find(c => c.type === type);
  return comp?.label || type;
}

// 获取组件渲染组件
function getElementComponent(type: string) {
  const componentMap: Record<string, any> = {
    text: markRaw(() => import('./components/TextElement.vue').then(m => m.default)),
    input: markRaw(() => import('./components/InputElement.vue').then(m => m.default)),
    button: markRaw(() => import('./components/ButtonElement.vue').then(m => m.default)),
    select: markRaw(() => import('./components/SelectElement.vue').then(m => m.default)),
    date: markRaw(() => import('./components/DateElement.vue').then(m => m.default)),
    checkbox: markRaw(() => import('./components/CheckboxElement.vue').then(m => m.default)),
    radio: markRaw(() => import('./components/RadioElement.vue').then(m => m.default)),
    table: markRaw(() => import('./components/TableElement.vue').then(m => m.default)),
    form: markRaw(() => import('./components/FormElement.vue').then(m => m.default)),
    list: markRaw(() => import('./components/ListElement.vue').then(m => m.default)),
    chart: markRaw(() => import('./components/ChartElement.vue').then(m => m.default)),
    card: markRaw(() => import('./components/CardElement.vue').then(m => m.default)),
    grid: markRaw(() => import('./components/GridElement.vue').then(m => m.default)),
    divider: markRaw(() => import('./components/DividerElement.vue').then(m => m.default)),
    space: markRaw(() => import('./components/SpaceElement.vue').then(m => m.default)),
  };
  return componentMap[type] || componentMap.text;
}

// 拖拽开始
function onDragStart(event: DragEvent, component: any) {
  draggingComponent.value = component;
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'copy';
    event.dataTransfer.setData('text/plain', component.type);
  }
}

// 拖拽经过
function onDragOver(event: DragEvent) {
  event.preventDefault();
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'copy';
  }
}

// 放置
function onDrop(event: DragEvent) {
  event.preventDefault();
  if (!draggingComponent.value) return;

  const newElement: PageElement = {
    id: Date.now().toString(),
    type: draggingComponent.value.type,
    name: draggingComponent.value.label,
    props: getDefaultProps(draggingComponent.value.type),
  };

  pageElements.value.push(newElement);
  saveHistory();
  draggingComponent.value = null;
}

// 获取默认属性
function getDefaultProps(type: string): Record<string, any> {
  const defaults: Record<string, Record<string, any>> = {
    text: { text: '新文本', fontSize: 14, color: '#1f2329' },
    input: { placeholder: '请输入', required: false },
    button: { text: '按钮', theme: 'primary' },
    select: { placeholder: '请选择', options: [] },
    date: { placeholder: '选择日期' },
    checkbox: { label: '选项', checked: false },
    radio: { label: '选项', checked: false },
    table: { dataSource: '', border: true, showIndex: true },
    form: { labelWidth: 100 },
    list: { data: [] },
    chart: { type: 'bar', data: [] },
    card: { title: '', border: true, hoverShadow: false },
    grid: { columns: 2, gutter: 16 },
    divider: { dashed: false, content: '' },
    space: { direction: 'horizontal', size: 16 },
  };
  return defaults[type] || {};
}

// 选择元素
function selectElement(element: PageElement) {
  selectedElementId.value = element.id;
}

// 复制元素
function handleCopy(index: number) {
  const element = pageElements.value[index];
  const newElement: PageElement = {
    ...JSON.parse(JSON.stringify(element)),
    id: Date.now().toString(),
    name: `${element.name} (副本)`,
  };
  pageElements.value.splice(index + 1, 0, newElement);
  saveHistory();
}

// 上移
function handleMoveUp(index: number) {
  if (index === 0) return;
  const temp = pageElements.value[index];
  pageElements.value[index] = pageElements.value[index - 1];
  pageElements.value[index - 1] = temp;
  saveHistory();
}

// 下移
function handleMoveDown(index: number) {
  if (index === pageElements.value.length - 1) return;
  const temp = pageElements.value[index];
  pageElements.value[index] = pageElements.value[index + 1];
  pageElements.value[index + 1] = temp;
  saveHistory();
}

// 删除元素
function handleDelete(index: number) {
  pageElements.value.splice(index, 1);
  if (selectedElementId.value === pageElements.value[index]?.id) {
    selectedElementId.value = null;
  }
  saveHistory();
}

// 保存历史记录
function saveHistory() {
  // 清除当前位置之后的历史
  history.value = history.value.slice(0, historyIndex.value + 1);
  // 添加新的历史记录
  history.value.push(JSON.parse(JSON.stringify(pageElements.value)));
  historyIndex.value++;
  
  // 限制历史记录数量
  if (history.value.length > 50) {
    history.value.shift();
    historyIndex.value--;
  }
}

// 撤销
function handleUndo() {
  if (!canUndo.value) return;
  historyIndex.value--;
  pageElements.value = JSON.parse(JSON.stringify(history.value[historyIndex.value]));
}

// 重做
function handleRedo() {
  if (!canRedo.value) return;
  historyIndex.value++;
  pageElements.value = JSON.parse(JSON.stringify(history.value[historyIndex.value]));
}

// 预览
function handlePreview() {
  MessagePlugin.info('预览功能开发中...');
}

// 保存
function handleSave() {
  if (!pageName.value.trim()) {
    MessagePlugin.warning('请输入页面名称');
    return;
  }
  
  const pageData = {
    name: pageName.value,
    elements: pageElements.value,
  };
  
  console.log('保存页面:', pageData);
  MessagePlugin.success('页面保存成功');
}

// 返回
function goBack() {
  router.push('/lowcode/entity');
}
</script>

<style scoped lang="less">
.page-designer {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f6f8;
}

.designer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .page-name-input {
      font-size: 18px;
      font-weight: 600;
      color: #1f2329;
      border: none;
      outline: none;
      width: 200px;
    }
  }
  
  .header-right {
    display: flex;
    gap: 8px;
  }
}

.designer-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.component-panel {
  width: 240px;
  background: #fff;
  border-right: 1px solid #e5e6eb;
  display: flex;
  flex-direction: column;
  
  .panel-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 16px;
    font-size: 14px;
    font-weight: 500;
    color: #1f2329;
    border-bottom: 1px solid #e5e6eb;
  }
  
  .panel-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px;
  }
  
  .component-group {
    margin-bottom: 20px;
    
    .group-title {
      font-size: 12px;
      color: #8f959e;
      margin-bottom: 8px;
      padding-left: 8px;
    }
  }
  
  .component-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    margin-bottom: 4px;
    border-radius: 6px;
    cursor: grab;
    transition: all 0.2s;
    
    &:hover {
      background: #f5f6f8;
    }
    
    &:active {
      cursor: grabbing;
    }
    
    .component-icon {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f6f8;
      border-radius: 6px;
      color: #646a73;
    }
    
    span {
      font-size: 13px;
      color: #1f2329;
    }
  }
}

.canvas-area {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  
  .canvas-container {
    max-width: 1200px;
    margin: 0 auto;
    min-height: 500px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
    padding: 24px;
    min-height: calc(100vh - 160px);
  }
  
  .canvas-element {
    position: relative;
    padding: 16px;
    margin-bottom: 16px;
    border-radius: 8px;
    border: 2px solid transparent;
    transition: all 0.2s;
    
    &:hover {
      border-color: #e5e6eb;
    }
    
    &.selected {
      border-color: #1677ff;
      background: #f5faff;
    }
    
    .element-actions {
      position: absolute;
      top: -8px;
      right: -8px;
      display: flex;
      gap: 4px;
      background: #fff;
      border-radius: 4px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      padding: 4px;
    }
  }
  
  .empty-canvas {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 100px 0;
    color: #8f959e;
    
    p {
      margin: 12px 0 4px 0;
      font-size: 14px;
    }
    
    .empty-hint {
      font-size: 12px;
      color: #bbbfc4;
    }
  }
}

.property-panel {
  width: 320px;
  background: #fff;
  border-left: 1px solid #e5e6eb;
  display: flex;
  flex-direction: column;
  
  .panel-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 16px;
    font-size: 14px;
    font-weight: 500;
    color: #1f2329;
    border-bottom: 1px solid #e5e6eb;
  }
  
  .panel-content {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    
    &.empty-property {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #8f959e;
      
      p {
        margin-top: 12px;
        font-size: 14px;
      }
    }
  }
}
</style>
