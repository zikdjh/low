<template>
  <div class="page-designer">
    <!-- 顶部工具栏 -->
    <div class="designer-header">
      <div class="header-left">
        <t-button variant="text" @click="goBack">
          <template #icon><ChevronLeftIcon /></template>
        </t-button>
        <input 
          v-model="pageName" 
          class="page-name-input"
          placeholder="请输入页面名称"
        />
      </div>
      <div class="header-right">
        <t-space :size="8">
          <t-tooltip content="撤销 (Ctrl+Z)">
            <t-button variant="outline" @click="handleUndo" :disabled="!canUndo">
              <template #icon><RollbackIcon /></template>
            </t-button>
          </t-tooltip>
          <t-tooltip content="重做 (Ctrl+Y)">
            <t-button variant="outline" @click="handleRedo" :disabled="!canRedo">
                <template #icon><ForwardIcon /></template>
              </t-button>
          </t-tooltip>
          <t-divider layout="vertical" />
          <t-button variant="outline" @click="handlePreview">
            <template #icon><BrowseIcon /></template>
            预览
          </t-button>
          <t-button theme="primary" @click="handleSave">
            <template #icon><SaveIcon /></template>
            保存页面
          </t-button>
        </t-space>
      </div>
    </div>

    <div class="designer-body">
      <!-- 左侧组件面板 -->
      <div class="component-panel">
        <div class="panel-header">
          <AppIcon size="16" />
          <span>组件库</span>
        </div>
        <div class="panel-content">
          <!-- 基础组件 -->
          <div class="component-group">
            <div class="group-title">基础</div>
            <div 
              v-for="comp in basicComponents" 
              :key="comp.type"
              class="component-item"
              draggable="true"
              @dragstart="onDragStart($event, comp)"
              @dragend="onDragEnd"
            >
              <div class="component-icon" :style="{ background: comp.bgColor }">
                <component :is="comp.iconComponent" size="18" />
              </div>
              <span>{{ comp.label }}</span>
            </div>
          </div>

          <!-- 数据组件 -->
          <div class="component-group">
            <div class="group-title">数据</div>
            <div 
              v-for="comp in dataComponents" 
              :key="comp.type"
              class="component-item"
              draggable="true"
              @dragstart="onDragStart($event, comp)"
              @dragend="onDragEnd"
            >
              <div class="component-icon" :style="{ background: comp.bgColor }">
                <component :is="comp.iconComponent" size="18" />
              </div>
              <span>{{ comp.label }}</span>
            </div>
          </div>

          <!-- 布局组件 -->
          <div class="component-group">
            <div class="group-title">布局</div>
            <div 
              v-for="comp in layoutComponents" 
              :key="comp.type"
              class="component-item"
              draggable="true"
              @dragstart="onDragStart($event, comp)"
              @dragend="onDragEnd"
            >
              <div class="component-icon" :style="{ background: comp.bgColor }">
                <component :is="comp.iconComponent" size="18" />
              </div>
              <span>{{ comp.label }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间画布区域 -->
      <div 
        class="canvas-area"
        :class="{ 'drag-over': isDraggingOver }"
        @dragover.prevent="onDragOver"
        @dragenter="onDragEnter"
        @dragleave="onDragLeave"
        @drop="onDrop"
      >
        <div class="canvas-container">
          <div
            v-for="(element, index) in pageElements"
            :key="element.id"
            class="canvas-element"
            :class="{ 
              selected: selectedElementId === element.id,
              'dragging-element': dragSortIndex === index
            }"
            draggable="true"
            @click.stop="selectElement(element)"
            @dragstart="onElementDragStart($event, index)"
            @dragover.prevent="onElementDragOver($event, index)"
            @drop="onElementDrop($event, index)"
            @dragend="onElementDragEnd"
          >
            <div class="drag-handle" @mousedown.stop>
              <MoveIcon size="14" />
            </div>
            <div class="element-wrapper">
              <component 
                :is="getElementComponent(element.type)" 
                :element="element"
              />
            </div>
            <div class="element-actions" v-if="selectedElementId === element.id">
              <t-tooltip content="复制">
                <t-button size="small" variant="text" theme="default" @click.stop="handleCopy(index)">
                  <CopyIcon size="14" />
                </t-button>
              </t-tooltip>
              <t-tooltip content="上移">
                <t-button size="small" variant="text" theme="default" @click.stop="handleMoveUp(index)" :disabled="index === 0">
                  <ChevronUpIcon size="14" />
                </t-button>
              </t-tooltip>
              <t-tooltip content="下移">
                <t-button size="small" variant="text" theme="default" @click.stop="handleMoveDown(index)" :disabled="index === pageElements.length - 1">
                  <ChevronDownIcon size="14" />
                </t-button>
              </t-tooltip>
              <t-divider layout="vertical" />
              <t-tooltip content="删除">
                <t-button size="small" variant="text" theme="danger" @click.stop="handleDelete(index)">
                  <DeleteIcon size="14" />
                </t-button>
              </t-tooltip>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="pageElements.length === 0" class="empty-canvas">
            <div class="empty-icon">
              <LayoutIcon size="48" />
            </div>
            <h3>开始设计您的页面</h3>
            <p>从左侧拖拽组件到此处</p>
            <div class="quick-add-buttons">
              <t-button size="small" variant="outline" @click="quickAdd('text')">
                <EditIcon /> 添加文本
              </t-button>
              <t-button size="small" variant="outline" @click="quickAdd('card')">
                <RectangleIcon /> 添加卡片
              </t-button>
              <t-button size="small" variant="outline" @click="quickAdd('button')">
                <ButtonIcon /> 添加按钮
              </t-button>
            </div>
          </div>
        </div>
        
        <!-- 拖拽提示 -->
        <div v-if="isDraggingOver" class="drop-hint">
          <DownloadIcon size="24" />
          <span>松开鼠标添加组件</span>
        </div>
      </div>

      <!-- 右侧属性面板 -->
      <div class="property-panel">
        <div class="panel-header">
          <SettingIcon size="16" />
          <span>属性配置</span>
        </div>
        <div class="panel-content" v-if="selectedElement">
          <div class="property-section">
            <div class="section-title">基础信息</div>
            <t-form label-width="80" size="small">
              <t-form-item label="组件ID">
                <t-input :value="selectedElement.id" disabled size="small" />
              </t-form-item>
              <t-form-item label="组件名称">
                <t-input v-model="selectedElement.name" size="small" placeholder="输入名称" />
              </t-form-item>
              <t-form-item label="组件类型">
                <t-tag variant="light" theme="primary">{{ getComponentLabel(selectedElement.type) }}</t-tag>
              </t-form-item>
            </t-form>
          </div>

          <t-divider />

          <div class="property-section">
            <div class="section-title">属性设置</div>
            <t-form label-width="80" size="small">
              <!-- 文本组件属性 -->
              <template v-if="selectedElement.type === 'text'">
                <t-form-item label="文本内容">
                  <t-textarea v-model="selectedElement.props.text" :autosize="{ minRows: 2 }" />
                </t-form-item>
                <t-form-item label="字体大小">
                  <t-slider v-model.number="selectedElement.props.fontSize" :min="12" :max="72" :show-tooltip="true" />
                </t-form-item>
                <t-form-item label="字体颜色">
                  <t-color-picker v-model="selectedElement.props.color" format="HEX" />
                </t-form-item>
                <t-form-item label="对齐方式">
                  <t-radio-group v-model="selectedElement.props.align" variant="default-filled">
                    <t-radio-button value="left">左</t-radio-button>
                    <t-radio-button value="center">中</t-radio-button>
                    <t-radio-button value="right">右</t-radio-button>
                  </t-radio-group>
                </t-form-item>
              </template>

              <!-- 输入框组件属性 -->
              <template v-else-if="selectedElement.type === 'input'">
                <t-form-item label="占位文本">
                  <t-input v-model="selectedElement.props.placeholder" placeholder="输入占位文本" />
                </t-form-item>
                <t-form-item label="是否必填">
                  <t-switch v-model="selectedElement.props.required" />
                </t-form-item>
                <t-form-item label="最大长度">
                  <t-input-number v-model="selectedElement.props.maxLength" :min="0" :max="1000" />
                </t-form-item>
              </template>

              <!-- 按钮组件属性 -->
              <template v-else-if="selectedElement.type === 'button'">
                <t-form-item label="按钮文本">
                  <t-input v-model="selectedElement.props.text" placeholder="按钮文字" />
                </t-form-item>
                <t-form-item label="按钮主题">
                  <t-select v-model="selectedElement.props.theme">
                    <t-option value="primary" label="主色" />
                    <t-option value="success" label="成功" />
                    <t-option value="warning" label="警告" />
                    <t-option value="danger" label="危险" />
                    <t-option value="default" label="默认" />
                  </t-select>
                </t-form-item>
                <t-form-item label="按钮尺寸">
                  <t-select v-model="selectedElement.props.size">
                    <t-option value="small" label="小" />
                    <t-option value="medium" label="中" />
                    <t-option value="large" label="大" />
                  </t-select>
                </t-form-item>
              </template>

              <!-- 表格组件属性 -->
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
                <t-form-item label="斑马纹">
                  <t-switch v-model="selectedElement.props.stripe" />
                </t-form-item>
              </template>

              <!-- 卡片组件属性 -->
              <template v-else-if="selectedElement.type === 'card'">
                <t-form-item label="卡片标题">
                  <t-input v-model="selectedElement.props.title" placeholder="输入标题" />
                </t-form-item>
                <t-form-item label="显示边框">
                  <t-switch v-model="selectedElement.props.border" />
                </t-form-item>
                <t-form-item label="悬停阴影">
                  <t-switch v-model="selectedElement.props.hoverShadow" />
                </t-form-item>
              </template>

              <!-- 栅格组件属性 -->
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
                  <t-slider v-model.number="selectedElement.props.gutter" :min="0" :max="32" :step="4" />
                </t-form-item>
              </template>

              <!-- 其他组件的通用属性 -->
              <template v-else>
                <p class="no-properties-tip">该组件暂无可配置属性</p>
              </template>
            </t-form>
          </div>

          <t-divider />

          <div class="property-actions">
            <t-button theme="danger" variant="outline" block @click="deleteSelected">
              <DeleteIcon /> 删除此组件
            </t-button>
          </div>
        </div>

        <div class="panel-content empty-property" v-else>
          <MouseIcon size="40" />
          <h4>未选中组件</h4>
          <p>点击画布中的组件<br/>查看和编辑其属性</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, markRaw } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import {
  ChevronLeftIcon, RollbackIcon, ForwardIcon, BrowseIcon, SaveIcon,
  AppIcon, EditIcon, ButtonIcon, ChevronDownIcon, CalendarIcon,
  CheckCircleFilledIcon, CircleIcon, TableIcon, FormIcon, ViewListIcon,
  ChartBarIcon, LayoutIcon, MinusIcon, ExpandHorizontalIcon,
  MoveIcon, CopyIcon, ChevronUpIcon, DeleteIcon,
  SettingIcon, MouseIcon, DownloadIcon, RectangleIcon
} from 'tdesign-icons-vue-next';

const router = useRouter();

const pageName = ref('未命名页面');
const draggingComponent = ref<any>(null);
const isDraggingOver = ref(false);
const dragSortIndex = ref<number | null>(null);

interface PageElement {
  id: string;
  type: string;
  name: string;
  props: Record<string, any>;
}

const pageElements = ref<PageElement[]>([]);

const selectedElementId = ref<string | null>(null);
const selectedElement = computed(() => {
  return pageElements.value.find(el => el.id === selectedElementId.value) || null;
});

// 历史记录
const history = ref<PageElement[][]>([[]]);
const historyIndex = ref(0);

const canUndo = computed(() => historyIndex.value > 0);
const canRedo = computed(() => historyIndex.value < history.value.length - 1);

// 组件定义（使用正确的图标组件）
const basicComponents = [
  { type: 'text', label: '文本', icon: 'edit', iconComponent: markRaw(EditIcon), bgColor: '#e6f7ff' },
  { type: 'input', label: '输入框', icon: 'edit', iconComponent: markRaw(EditIcon), bgColor: '#f6ffed' },
  { type: 'button', label: '按钮', icon: 'button', iconComponent: markRaw(ButtonIcon), bgColor: '#fff7e6' },
  { type: 'select', label: '下拉框', icon: 'chevron-down', iconComponent: markRaw(ChevronDownIcon), bgColor: '#f9f0ff' },
  { type: 'date', label: '日期选择', icon: 'calendar', iconComponent: markRaw(CalendarIcon), bgColor: '#fff1f0' },
  { type: 'checkbox', label: '复选框', icon: 'check-circle-filled', iconComponent: markRaw(CheckCircleFilledIcon), bgColor: '#fffbe6' },
  { type: 'radio', label: '单选框', icon: 'circle', iconComponent: markRaw(CircleIcon), bgColor: '#e6fffb' },
];

const dataComponents = [
  { type: 'table', label: '数据表格', icon: 'table', iconComponent: markRaw(TableIcon), bgColor: '#e6f7ff' },
  { type: 'form', label: '表单', icon: 'form', iconComponent: markRaw(FormIcon), bgColor: '#f6ffed' },
  { type: 'list', label: '列表', icon: 'view-list', iconComponent: markRaw(ViewListIcon), bgColor: '#fff7e6' },
  { type: 'chart', label: '图表', icon: 'chart-bar', iconComponent: markRaw(ChartBarIcon), bgColor: '#f9f0ff' },
];

const layoutComponents = [
  { type: 'card', label: '卡片', icon: 'layout', iconComponent: markRaw(LayoutIcon), bgColor: '#fff1f0' },
  { type: 'grid', label: '栅格', icon: 'layout', iconComponent: markRaw(LayoutIcon), bgColor: '#e6f7ff' },
  { type: 'divider', label: '分割线', icon: 'minus', iconComponent: markRaw(MinusIcon), bgColor: '#f5f5f5' },
  { type: 'space', label: '间距', icon: 'expand-horizontal', iconComponent: markRaw(ExpandHorizontalIcon), bgColor: '#fffbe6' },
];

function getComponentLabel(type: string): string {
  const allComponents = [...basicComponents, ...dataComponents, ...layoutComponents];
  const comp = allComponents.find(c => c.type === type);
  return comp?.label || type;
}

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

function onDragStart(event: DragEvent, component: any) {
  draggingComponent.value = component;
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'copy';
    event.dataTransfer.setData('text/plain', JSON.stringify(component));
  }
  
  // 添加拖拽样式
  const target = event.target as HTMLElement;
  if (target) {
    target.classList.add('dragging');
  }
}

function onDragEnd() {
  draggingComponent.value = null;
  document.querySelectorAll('.dragging').forEach(el => el.classList.remove('dragging'));
}

function onDragOver(event: DragEvent) {
  event.preventDefault();
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'copy';
  }
}

function onDragEnter() {
  isDraggingOver.value = true;
}

function onDragLeave(event: DragEvent) {
  const relatedTarget = event.relatedTarget as Node | null;
  const currentTarget = event.currentTarget as HTMLElement;
  if (!relatedTarget || !currentTarget.contains(relatedTarget)) {
    isDraggingOver.value = false;
  }
}

function onDrop(event: DragEvent) {
  event.preventDefault();
  isDraggingOver.value = false;

  if (!draggingComponent.value) return;

  const newElement: PageElement = {
    id: `el_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    type: draggingComponent.value.type,
    name: `${draggingComponent.value.label}_${pageElements.value.length + 1}`,
    props: getDefaultProps(draggingComponent.value.type),
  };

  pageElements.value.push(newElement);
  selectedElementId.value = newElement.id;
  saveHistory();
  draggingComponent.value = null;

  MessagePlugin.success(`已添加 ${newElement.name}`);
}

function quickAdd(type: string) {
  const newElement: PageElement = {
    id: `el_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    type: type,
    name: `${getComponentLabel(type)}_${pageElements.value.length + 1}`,
    props: getDefaultProps(type),
  };

  pageElements.value.push(newElement);
  selectedElementId.value = newElement.id;
  saveHistory();

  MessagePlugin.success(`已添加 ${newElement.name}`);
}

function getDefaultProps(type: string): Record<string, any> {
  const defaults: Record<string, Record<string, any>> = {
    text: { text: '双击编辑文本内容', fontSize: 16, color: '#1f2329', align: 'left' },
    input: { placeholder: '请输入内容', required: false, maxLength: 255 },
    button: { text: '点击按钮', theme: 'primary', size: 'medium' },
    select: { placeholder: '请选择', options: [] },
    date: { placeholder: '选择日期' },
    checkbox: { label: '复选选项', checked: false },
    radio: { label: '单选选项', checked: false },
    table: { dataSource: '', border: true, showIndex: true, stripe: false },
    form: { labelWidth: 100 },
    list: { data: [] },
    chart: { type: 'bar', data: [] },
    card: { title: '卡片标题', border: true, hoverShadow: true },
    grid: { columns: 2, gutter: 16 },
    divider: { dashed: false, content: '' },
    space: { direction: 'horizontal', size: 16 },
  };
  return defaults[type] || {};
}

function selectElement(element: PageElement) {
  selectedElementId.value = element.id;
}

function handleCopy(index: number) {
  const element = pageElements.value[index];
  const newElement: PageElement = {
    ...JSON.parse(JSON.stringify(element)),
    id: `el_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    name: `${element.name}_副本`,
  };
  pageElements.value.splice(index + 1, 0, newElement);
  selectedElementId.value = newElement.id;
  saveHistory();
  MessagePlugin.success(`已复制 ${newElement.name}`);
}

function handleMoveUp(index: number) {
  if (index === 0) return;
  const temp = pageElements.value[index];
  pageElements.value[index] = pageElements.value[index - 1];
  pageElements.value[index - 1] = temp;
  saveHistory();
}

function handleMoveDown(index: number) {
  if (index >= pageElements.value.length - 1) return;
  const temp = pageElements.value[index];
  pageElements.value[index] = pageElements.value[index + 1];
  pageElements.value[index + 1] = temp;
  saveHistory();
}

function handleDelete(index: number) {
  const name = pageElements.value[index].name;
  pageElements.value.splice(index, 1);
  if (selectedElementId.value && !pageElements.value.find(el => el.id === selectedElementId.value)) {
    selectedElementId.value = null;
  }
  saveHistory();
  MessagePlugin.success(`已删除 ${name}`);
}

function deleteSelected() {
  if (!selectedElementId.value) return;
  const index = pageElements.value.findIndex(el => el.id === selectedElementId.value);
  if (index !== -1) {
    handleDelete(index);
  }
}

function onElementDragStart(event: DragEvent, index: number) {
  dragSortIndex.value = index;
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/plain', index.toString());
  }
}

function onElementDragOver(event: DragEvent, _index: number) {
  event.preventDefault();
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move';
  }
}

function onElementDrop(event: DragEvent, targetIndex: number) {
  event.preventDefault();
  
  if (dragSortIndex.value === null || dragSortIndex.value === targetIndex) return;
  
  const sourceIndex = dragSortIndex.value;
  const element = pageElements.value.splice(sourceIndex, 1)[0];
  pageElements.value.splice(targetIndex, 0, element);
  
  saveHistory();
  dragSortIndex.value = null;
}

function onElementDragEnd() {
  dragSortIndex.value = null;
}

function saveHistory() {
  history.value = history.value.slice(0, historyIndex.value + 1);
  history.value.push(JSON.parse(JSON.stringify(pageElements.value)));
  historyIndex.value++;
  
  if (history.value.length > 50) {
    history.value.shift();
    historyIndex.value--;
  }
}

function handleUndo() {
  if (!canUndo.value) return;
  historyIndex.value--;
  pageElements.value = JSON.parse(JSON.stringify(history.value[historyIndex.value]));
  MessagePlugin.info('撤销操作');
}

function handleRedo() {
  if (!canRedo.value) return;
  historyIndex.value++;
  pageElements.value = JSON.parse(JSON.stringify(history.value[historyIndex.value]));
  MessagePlugin.info('重做操作');
}

function handlePreview() {
  MessagePlugin.info('预览功能开发中...');
}

function handleSave() {
  if (!pageName.value.trim()) {
    MessagePlugin.warning('请输入页面名称');
    return;
  }
  
  const pageData = {
    name: pageName.value,
    elements: pageElements.value,
    updatedAt: new Date().toISOString(),
  };
  
  localStorage.setItem('lowcode_page_' + Date.now(), JSON.stringify(pageData));
  console.log('保存页面:', pageData);
  MessagePlugin.success('页面保存成功！');
}

function goBack() {
  router.push('/home');
}
</script>

<style scoped lang="less">
.page-designer {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f0f2f5;
}

.designer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 56px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .page-name-input {
      font-size: 16px;
      font-weight: 600;
      color: #fff;
      border: none;
      outline: none;
      background: transparent;
      width: 200px;
      
      &::placeholder {
        color: rgba(255, 255, 255, 0.7);
      }
    }
    
    :deep(.t-button) {
      color: rgba(255, 255, 255, 0.85);
      
      &:hover {
        color: #fff;
        background: rgba(255, 255, 255, 0.15);
      }
    }
  }
  
  .header-right {
    :deep(.t-button) {
      background: rgba(255, 255, 255, 0.15);
      border-color: rgba(255, 255, 255, 0.3);
      color: #fff;
      
      &:hover:not(:disabled) {
        background: rgba(255, 255, 255, 0.25);
        border-color: rgba(255, 255, 255, 0.5);
      }
      
      &.t-button--variant-base.theme-primary {
        background: #fff;
        color: #667eea;
        border-color: #fff;
        
        &:hover {
          background: rgba(255, 255, 255, 0.9);
        }
      }
    }
  }
}

.designer-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.component-panel {
  width: 260px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  
  .panel-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 16px;
    font-size: 14px;
    font-weight: 600;
    color: #1f2329;
    border-bottom: 1px solid #e8e8e8;
    background: #fafafa;
  }
  
  .panel-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px;
  }
  
  .component-group {
    margin-bottom: 16px;
    
    .group-title {
      font-size: 11px;
      font-weight: 600;
      color: #8f959e;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      margin-bottom: 8px;
      padding: 0 8px;
    }
  }
  
  .component-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    margin-bottom: 4px;
    border-radius: 8px;
    cursor: grab;
    transition: all 0.2s ease;
    border: 1px solid transparent;
    
    &:hover {
      background: #f5f7fa;
      border-color: #d9e2ec;
      transform: translateX(2px);
    }
    
    &:active {
      cursor: grabbing;
      transform: scale(0.98);
    }
    
    &.dragging {
      opacity: 0.5;
      background: #e6f7ff;
      border-color: #1890ff;
    }
    
    .component-icon {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      color: #5c6ac4;
      transition: transform 0.2s;
    }
    
    span {
      font-size: 13px;
      color: #1f2329;
      font-weight: 500;
    }
  }
}

.canvas-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  position: relative;
  background: #f0f2f5;
  transition: background 0.3s;
  
  &.drag-over {
    background: #e6f7ff;
    
    .canvas-container {
      border-color: #1890ff;
      box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
    }
  }
  
  .canvas-container {
    max-width: 900px;
    margin: 0 auto;
    min-height: calc(100vh - 140px);
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    padding: 24px;
    border: 2px dashed transparent;
    transition: all 0.3s;
  }
  
  .canvas-element {
    position: relative;
    padding: 16px;
    margin-bottom: 12px;
    border-radius: 8px;
    border: 2px solid transparent;
    background: #fafbfc;
    transition: all 0.2s ease;
    
    &:hover {
      border-color: #d9e2ec;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    }
    
    &.selected {
      border-color: #1677ff;
      background: #f0f5ff;
      box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.1);
    }
    
    &.dragging-element {
      opacity: 0.5;
      border-style: dashed;
      transform: scale(1.02);
    }
    
    .drag-handle {
      position: absolute;
      left: 4px;
      top: 50%;
      transform: translateY(-50%);
      cursor: move;
      opacity: 0;
      padding: 4px;
      border-radius: 4px;
      background: rgba(22, 119, 255, 0.1);
      color: #1677ff;
      transition: opacity 0.2s;
      
      &:hover {
        background: rgba(22, 119, 255, 0.2);
      }
    }
    
    &:hover .drag-handle {
      opacity: 1;
    }
    
    .element-wrapper {
      pointer-events: none;
    }
    
    .element-actions {
      position: absolute;
      top: -12px;
      right: -12px;
      display: flex;
      gap: 2px;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      padding: 6px;
      z-index: 10;
      border: 1px solid #e8e8e8;
      
      :deep(.t-button) {
        width: 28px;
        height: 28px;
        padding: 0;
      }
    }
  }

  .ghost-class {
    opacity: 0.5;
    background: #e6f7ff;
    border: 2px dashed #1677ff;
  }
  
  .drop-hint {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    padding: 32px 48px;
    background: rgba(22, 119, 255, 0.95);
    color: #fff;
    border-radius: 16px;
    font-size: 16px;
    font-weight: 500;
    box-shadow: 0 8px 32px rgba(22, 119, 255, 0.4);
    z-index: 1000;
    pointer-events: none;
    animation: pulse 1s ease-in-out infinite;
  }
  
  .empty-canvas {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    color: #8f959e;
    
    .empty-icon {
      width: 96px;
      height: 96px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
      border-radius: 24px;
      margin-bottom: 20px;
      color: #667eea;
    }
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: #1f2329;
      margin: 0 0 8px 0;
    }
    
    p {
      margin: 0 0 24px 0;
      font-size: 14px;
    }
    
    .quick-add-buttons {
      display: flex;
      gap: 12px;
    }
  }
}

.property-panel {
  width: 300px;
  background: #fff;
  border-left: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  
  .panel-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 16px;
    font-size: 14px;
    font-weight: 600;
    color: #1f2329;
    border-bottom: 1px solid #e8e8e8;
    background: #fafafa;
  }
  
  .panel-content {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    
    .property-section {
      margin-bottom: 16px;
      
      .section-title {
        font-size: 12px;
        font-weight: 600;
        color: #8f959e;
        margin-bottom: 12px;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }
      
      .no-properties-tip {
        text-align: center;
        color: #bbbfc4;
        font-size: 13px;
        padding: 20px 0;
      }
    }
    
    .property-actions {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #e8e8e8;
    }
  }
  
  .empty-property {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    color: #8f959e;
    padding: 60px 20px;
    
    h4 {
      font-size: 16px;
      font-weight: 500;
      color: #1f2329;
      margin: 16px 0 8px 0;
    }
    
    p {
      font-size: 13px;
      line-height: 1.6;
      margin: 0;
    }
  }
}

@keyframes pulse {
  0%, 100% { 
    transform: translate(-50%, -50%) scale(1); 
    opacity: 1; 
  }
  50% { 
    transform: translate(-50%, -50%) scale(1.05); 
    opacity: 0.9; 
  }
}
</style>
