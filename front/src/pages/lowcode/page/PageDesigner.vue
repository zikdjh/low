<template>
  <div class="page-designer">
    <!-- 顶部工具栏 -->
    <div class="designer-header">
      <div class="header-left">
        <t-button variant="text" @click="goBack" class="back-btn">
          <template #icon><ChevronLeftIcon /></template>
          返回主页
        </t-button>
        <div class="page-info">
          <input 
            v-model="pageName" 
            class="page-name-input"
            placeholder="请输入页面名称"
          />
          <span class="page-status">草稿</span>
        </div>
      </div>
      <div class="header-center">
        <div class="view-tabs">
          <button 
            v-for="tab in viewTabs" 
            :key="tab.key"
            :class="['view-tab', { active: currentView === tab.key }]"
            @click="currentView = tab.key"
          >
            <component :is="tab.icon" size="16" />
            {{ tab.label }}
          </button>
        </div>
      </div>
      <div class="header-right">
        <t-space :size="8">
          <t-tooltip content="撤销 (Ctrl+Z)">
            <t-button 
              variant="outline" 
              @click="handleUndo" 
              :disabled="!canUndo"
              class="toolbar-btn"
            >
              <template #icon><RollbackIcon /></template>
            </t-button>
          </t-tooltip>
          <t-tooltip content="重做 (Ctrl+Y)">
            <t-button 
              variant="outline" 
              @click="handleRedo" 
              :disabled="!canRedo"
              class="toolbar-btn"
            >
                <template #icon><ForwardIcon /></template>
              </t-button>
          </t-tooltip>
          <t-divider layout="vertical" />
          <t-button variant="outline" @click="handlePreview" class="toolbar-btn">
            <template #icon><BrowseIcon /></template>
            预览
          </t-button>
          <t-button theme="primary" @click="handleSave" class="save-btn">
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
          <component :is="componentPanelIcon" size="16" />
          <span>组件库</span>
          <t-button variant="text" size="small" @click="togglePanelCollapse" class="collapse-btn">
            <ChevronLeftIcon v-if="!panelCollapsed" />
            <ChevronRightIcon v-else />
          </t-button>
        </div>
        
        <div class="panel-content" :class="{ collapsed: panelCollapsed }">
          <div v-for="group in componentGroups" :key="group.name" class="component-group">
            <div class="group-header" @click="toggleGroup(group.name)">
              <component :is="group.icon" size="14" />
              <span>{{ group.label }}</span>
              <ChevronDownIcon 
                :class="{ rotated: expandedGroups.includes(group.name) }" 
                size="14" 
              />
            </div>
            
            <div v-show="expandedGroups.includes(group.name)" class="group-items">
              <div 
                v-for="comp in group.components" 
                :key="comp.type"
                class="component-item"
                draggable="true"
                @dragstart="onDragStart($event, comp)"
                @dragend="onDragEnd"
              >
                <div class="component-icon-wrapper">
                  <component :is="comp.iconComponent" size="18" />
                </div>
                <span class="component-label">{{ comp.label }}</span>
              </div>
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
          <div class="canvas-header">
            <div class="canvas-title">{{ pageName || '未命名页面' }}</div>
            <div class="canvas-actions">
              <t-button variant="text" size="small" @click="handleClear">
                <DeleteIcon size="14" /> 清空画布
              </t-button>
            </div>
          </div>
          
          <div class="canvas-content">
            <div
              v-for="(element, index) in pageElements"
              :key="element.id"
              class="canvas-element"
              :class="{ 
                selected: selectedElementId === element.id,
                'is-dragging': draggingElementId === element.id,
              }"
              :style="{
                left: element.x + 'px',
                top: element.y + 'px',
                width: element.width + 'px',
                height: element.height + 'px',
              }"
              @click.stop="selectElement(element)"
              @mousedown="onMouseDown($event, element)"
            >
              <div class="element-header">
                <div class="drag-handle" @mousedown.stop>
                  <VerticalIcon size="14" />
                </div>
                <div class="element-type">
                  <component :is="getElementIcon(element.type)" size="12" />
                  <span>{{ getComponentLabel(element.type) }}</span>
                </div>
                <div class="element-actions" v-if="selectedElementId === element.id">
                  <t-button 
                    size="small" 
                    variant="text" 
                    @click.stop="handleCopy(index)"
                    :title="copyTitle"
                  >
                    <CopyIcon size="12" />
                  </t-button>
                  <t-button 
                    size="small" 
                    variant="text" 
                    @click.stop="handleMoveUp(index)"
                    :disabled="index === 0"
                    :title="moveUpTitle"
                  >
                    <ChevronUpIcon size="12" />
                  </t-button>
                  <t-button 
                    size="small" 
                    variant="text" 
                    @click.stop="handleMoveDown(index)"
                    :disabled="index === pageElements.length - 1"
                    :title="moveDownTitle"
                  >
                    <ChevronDownIcon size="12" />
                  </t-button>
                  <t-divider layout="vertical" />
                  <t-button 
                    size="small" 
                    variant="text" 
                    theme="danger"
                    @click.stop="handleDelete(index)"
                    :title="deleteTitle"
                  >
                    <DeleteIcon size="12" />
                  </t-button>
                </div>
              </div>
              
              <div class="element-content">
                <component 
                  :is="getElementComponent(element.type)" 
                  :element="element"
                />
              </div>
              
              <div class="element-resize-handle" @mousedown.stop="onResizeStart($event, element)"></div>
            </div>

            <!-- 空状态 -->
            <div v-if="pageElements.length === 0" class="empty-canvas">
              <div class="empty-illustration">
                <div class="illustration-bg"></div>
                <div class="illustration-icon">
                  <LayoutIcon size="64" />
                </div>
              </div>
              <h3>开始设计您的页面</h3>
              <p>从左侧拖拽组件到此处，或者点击下方按钮快速添加</p>
              <div class="quick-add-buttons">
                <t-button size="small" variant="outline" @click="quickAdd('text')" class="quick-btn">
                  <EditIcon /> 添加文本
                </t-button>
                <t-button size="small" variant="outline" @click="quickAdd('card')" class="quick-btn">
                  <RectangleIcon /> 添加卡片
                </t-button>
                <t-button size="small" variant="outline" @click="quickAdd('button')" class="quick-btn">
                  <ButtonIcon /> 添加按钮
                </t-button>
                <t-button size="small" variant="outline" @click="quickAdd('table')" class="quick-btn">
                  <TableIcon /> 添加表格
                </t-button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 拖拽提示 -->
        <Transition name="fade">
          <div v-if="isDraggingOver" class="drop-hint">
            <div class="hint-icon">
              <DownloadIcon size="32" />
            </div>
            <div class="hint-text">松开鼠标添加组件</div>
            <div class="hint-shortcut">或按 Enter 键快速添加</div>
          </div>
        </Transition>
      </div>

      <!-- 预览视图 -->
      <div v-if="currentView === 'preview'" class="preview-area">
        <div class="preview-container">
          <div class="preview-header">
            <div class="preview-title">{{ pageName || '未命名页面' }}</div>
            <div class="preview-actions">
              <t-button variant="outline" size="small" @click="currentView = 'design'">
                <EditIcon size="14" /> 返回设计
              </t-button>
            </div>
          </div>
          
          <div class="preview-content">
            <div
              v-for="element in pageElements"
              :key="element.id"
              class="preview-element"
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
              />
            </div>
            
            <div v-if="pageElements.length === 0" class="empty-preview">
              <LayoutIcon size="64" />
              <p>页面尚未添加任何组件</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧属性面板 -->
      <div class="property-panel" :class="{ collapsed: propertyPanelCollapsed }">
        <div class="panel-header">
          <SettingIcon size="16" />
          <span>属性配置</span>
          <t-button 
            variant="text" 
            size="small" 
            @click="togglePropertyPanelCollapse" 
            class="collapse-btn"
          >
            <ChevronRightIcon v-if="!propertyPanelCollapsed" />
            <ChevronLeftIcon v-else />
          </t-button>
        </div>
        
        <div class="panel-content" :class="{ collapsed: propertyPanelCollapsed }">
          <div v-if="selectedElement" class="property-form">
            <!-- 基础信息 -->
            <div class="property-section">
              <div class="section-header">
                <div class="section-title">基础信息</div>
              </div>
              <div class="section-content">
                <t-form label-width="70" size="small">
                  <t-form-item label="组件ID">
                    <t-input :value="selectedElement.id" disabled size="small" class="id-input" />
                  </t-form-item>
                  <t-form-item label="组件名称">
                    <t-input v-model="selectedElement.name" size="small" placeholder="输入名称" />
                  </t-form-item>
                  <t-form-item label="组件类型">
                    <t-tag variant="light" theme="primary" class="type-tag">
                      {{ getComponentLabel(selectedElement.type) }}
                    </t-tag>
                  </t-form-item>
                </t-form>
              </div>
            </div>

            <t-divider />

            <!-- 属性设置 -->
            <div class="property-section">
              <div class="section-header">
                <div class="section-title">属性设置</div>
                <t-button variant="text" size="small" @click="resetProps" class="reset-btn">
                  重置
                </t-button>
              </div>
              <div class="section-content">
                <t-form label-width="70" size="small">
                  <!-- 文本组件属性 -->
                  <template v-if="selectedElement.type === 'text'">
                    <t-form-item label="文本内容">
                      <t-textarea 
                        v-model="selectedElement.props.text" 
                        :autosize="{ minRows: 2, maxRows: 4 }"
                        placeholder="输入文本内容"
                      />
                    </t-form-item>
                    <t-form-item label="字体大小">
                      <div class="slider-control">
                        <t-slider 
                          v-model.number="selectedElement.props.fontSize" 
                          :min="12" 
                          :max="72" 
                          :show-tooltip="true"
                          :marks="{ 12: '12px', 24: '24px', 36: '36px', 48: '48px', 72: '72px' }"
                        />
                        <span class="slider-value">{{ selectedElement.props.fontSize }}px</span>
                      </div>
                    </t-form-item>
                    <t-form-item label="字体颜色">
                      <div class="color-picker-wrapper">
                        <t-color-picker 
                          v-model="selectedElement.props.color" 
                          format="HEX" 
                        />
                        <t-input 
                          v-model="selectedElement.props.color" 
                          size="small" 
                          class="color-input"
                        />
                      </div>
                    </t-form-item>
                    <t-form-item label="对齐方式">
                      <t-radio-group v-model="selectedElement.props.align" variant="default-filled">
                        <t-radio-button value="left"><AlignTopIcon /></t-radio-button>
                        <t-radio-button value="center"><AlignCenterIcon /></t-radio-button>
                        <t-radio-button value="right"><AlignTopIcon /></t-radio-button>
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
                    <t-form-item label="输入类型">
                      <t-select v-model="selectedElement.props.type">
                        <t-option value="text" label="文本" />
                        <t-option value="password" label="密码" />
                        <t-option value="email" label="邮箱" />
                        <t-option value="number" label="数字" />
                      </t-select>
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
                    <t-form-item label="按钮变体">
                      <t-select v-model="selectedElement.props.variant">
                        <t-option value="base" label="实心" />
                        <t-option value="outline" label="描边" />
                        <t-option value="text" label="文字" />
                      </t-select>
                    </t-form-item>
                  </template>

                  <!-- 表格组件属性 -->
                  <template v-else-if="selectedElement.type === 'table'">
                    <t-form-item label="数据源">
                      <t-select v-model="selectedElement.props.dataSource">
                        <t-option value="" label="请选择数据源" />
                        <t-option value="mock1" label="示例数据1" />
                        <t-option value="mock2" label="示例数据2" />
                        <t-option value="mock3" label="示例数据3" />
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
                    <t-form-item label="分页显示">
                      <t-switch v-model="selectedElement.props.pagination" />
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
                    <t-form-item label="可折叠">
                      <t-switch v-model="selectedElement.props.collapsible" />
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
                        <t-option :value="6" label="6列" />
                      </t-select>
                    </t-form-item>
                    <t-form-item label="间距">
                      <t-slider 
                        v-model.number="selectedElement.props.gutter" 
                        :min="0" 
                        :max="48" 
                        :step="4"
                        :show-tooltip="true"
                      />
                    </t-form-item>
                  </template>

                  <!-- 下拉框组件属性 -->
                  <template v-else-if="selectedElement.type === 'select'">
                    <t-form-item label="占位文本">
                      <t-input v-model="selectedElement.props.placeholder" placeholder="请选择" />
                    </t-form-item>
                    <t-form-item label="多选模式">
                      <t-switch v-model="selectedElement.props.multiple" />
                    </t-form-item>
                  </template>

                  <!-- 日期选择器属性 -->
                  <template v-else-if="selectedElement.type === 'date'">
                    <t-form-item label="占位文本">
                      <t-input v-model="selectedElement.props.placeholder" placeholder="选择日期" />
                    </t-form-item>
                    <t-form-item label="选择范围">
                      <t-switch v-model="selectedElement.props.range" />
                    </t-form-item>
                  </template>

                  <!-- 复选框组件属性 -->
                  <template v-else-if="selectedElement.type === 'checkbox'">
                    <t-form-item label="选项标签">
                      <t-input v-model="selectedElement.props.label" placeholder="选项标签" />
                    </t-form-item>
                    <t-form-item label="默认选中">
                      <t-switch v-model="selectedElement.props.checked" />
                    </t-form-item>
                  </template>

                  <!-- 单选框组件属性 -->
                  <template v-else-if="selectedElement.type === 'radio'">
                    <t-form-item label="选项标签">
                      <t-input v-model="selectedElement.props.label" placeholder="选项标签" />
                    </t-form-item>
                    <t-form-item label="默认选中">
                      <t-switch v-model="selectedElement.props.checked" />
                    </t-form-item>
                  </template>

                  <!-- 其他组件的通用属性 -->
                  <template v-else>
                    <div class="no-properties-tip">
                      <InkIcon size="24" />
                      <p>该组件暂无可配置属性</p>
                    </div>
                  </template>
                </t-form>
              </div>
            </div>

            <t-divider />

            <!-- 操作按钮 -->
            <div class="property-actions">
              <t-button theme="danger" variant="outline" block @click="deleteSelected">
                <DeleteIcon /> 删除此组件
              </t-button>
            </div>
          </div>

          <!-- 未选中状态 -->
          <div v-else class="empty-property">
            <div class="empty-icon">
              <MousePointerIcon size="48" />
            </div>
            <h4>未选中组件</h4>
            <p>点击画布中的组件<br/>查看和编辑其属性</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, markRaw, defineAsyncComponent } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import {
  ChevronLeftIcon, ChevronRightIcon, ChevronUpIcon, ChevronDownIcon,
  RollbackIcon, ForwardIcon, BrowseIcon, SaveIcon,
  AppIcon, EditIcon, ButtonIcon, CalendarIcon,
  CheckCircleFilledIcon, CircleIcon, TableIcon, FormIcon, ViewListIcon,
  ChartBarIcon, LayoutIcon, MinusIcon, ExpandHorizontalIcon,
  VerticalIcon, CopyIcon, DeleteIcon,
  SettingIcon, DownloadIcon, RectangleIcon,
  AlignTopIcon, InkIcon, TapeIcon, ConstraintIcon
} from 'tdesign-icons-vue-next';
const MousePointerIcon = { name: 'MousePointerIcon' };
const AlignCenterIcon = { name: 'AlignCenterIcon' };

const router = useRouter();

// 视图切换
const currentView = ref('design');
const viewTabs = [
  { key: 'design', label: '设计', icon: LayoutIcon },
  { key: 'preview', label: '预览', icon: BrowseIcon },
];

// 面板折叠状态
const panelCollapsed = ref(false);
const propertyPanelCollapsed = ref(false);
const expandedGroups = ref(['basic']);

// 拖拽状态
const draggingComponent = ref<any>(null);
const isDraggingOver = ref(false);

// 页面状态
const pageName = ref('未命名页面');

interface PageElement {
  id: string;
  type: string;
  name: string;
  props: Record<string, any>;
  x: number;
  y: number;
  width: number;
  height: number;
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

// 工具提示
const copyTitle = '复制组件';
const moveUpTitle = '上移';
const moveDownTitle = '下移';
const deleteTitle = '删除';

// 自由拖拽状态
const draggingElementId = ref<string | null>(null);
const draggingElement = ref<PageElement | null>(null);
const dragOffset = ref({ x: 0, y: 0 });
const isResizing = ref(false);
const resizeStartPos = ref({ x: 0, y: 0 });
const resizeStartSize = ref({ width: 0, height: 0 });

// 组件面板图标
const componentPanelIcon = computed(() => {
  return panelCollapsed.value ? ChevronRightIcon : AppIcon;
});

// 组件分组定义
const componentGroups = [
  {
    name: 'basic',
    label: '基础组件',
    icon: TapeIcon,
    components: [
      { type: 'text', label: '文本', iconComponent: markRaw(EditIcon) },
      { type: 'input', label: '输入框', iconComponent: markRaw(TapeIcon) },
      { type: 'button', label: '按钮', iconComponent: markRaw(ButtonIcon) },
      { type: 'select', label: '下拉框', iconComponent: markRaw(ChevronDownIcon) },
      { type: 'date', label: '日期选择', iconComponent: markRaw(CalendarIcon) },
      { type: 'checkbox', label: '复选框', iconComponent: markRaw(CheckCircleFilledIcon) },
      { type: 'radio', label: '单选框', iconComponent: markRaw(CircleIcon) },
    ]
  },
  {
    name: 'data',
    label: '数据组件',
    icon: TableIcon,
    components: [
      { type: 'table', label: '数据表格', iconComponent: markRaw(TableIcon) },
      { type: 'form', label: '表单', iconComponent: markRaw(FormIcon) },
      { type: 'list', label: '列表', iconComponent: markRaw(ViewListIcon) },
      { type: 'chart', label: '图表', iconComponent: markRaw(ChartBarIcon) },
    ]
  },
  {
    name: 'layout',
    label: '布局组件',
    icon: ConstraintIcon,
    components: [
      { type: 'card', label: '卡片', iconComponent: markRaw(LayoutIcon) },
      { type: 'grid', label: '栅格', iconComponent: markRaw(LayoutIcon) },
      { type: 'divider', label: '分割线', iconComponent: markRaw(MinusIcon) },
      { type: 'space', label: '间距', iconComponent: markRaw(ExpandHorizontalIcon) },
    ]
  },
];

function togglePanelCollapse() {
  panelCollapsed.value = !panelCollapsed.value;
}

function togglePropertyPanelCollapse() {
  propertyPanelCollapsed.value = !propertyPanelCollapsed.value;
}

function toggleGroup(name: string) {
  const index = expandedGroups.value.indexOf(name);
  if (index > -1) {
    expandedGroups.value.splice(index, 1);
  } else {
    expandedGroups.value.push(name);
  }
}

function getComponentLabel(type: string): string {
  for (const group of componentGroups) {
    const comp = group.components.find(c => c.type === type);
    if (comp) return comp.label;
  }
  return type;
}

function getElementIcon(type: string) {
  for (const group of componentGroups) {
    const comp = group.components.find(c => c.type === type);
    if (comp) return comp.iconComponent;
  }
  return TapeIcon;
}

function getElementComponent(type: string) {
  const componentMap: Record<string, any> = {
    text: defineAsyncComponent(() => import('./components/TextElement.vue')),
    input: defineAsyncComponent(() => import('./components/InputElement.vue')),
    button: defineAsyncComponent(() => import('./components/ButtonElement.vue')),
    select: defineAsyncComponent(() => import('./components/SelectElement.vue')),
    date: defineAsyncComponent(() => import('./components/DateElement.vue')),
    checkbox: defineAsyncComponent(() => import('./components/CheckboxElement.vue')),
    radio: defineAsyncComponent(() => import('./components/RadioElement.vue')),
    table: defineAsyncComponent(() => import('./components/TableElement.vue')),
    form: defineAsyncComponent(() => import('./components/FormElement.vue')),
    list: defineAsyncComponent(() => import('./components/ListElement.vue')),
    chart: defineAsyncComponent(() => import('./components/ChartElement.vue')),
    card: defineAsyncComponent(() => import('./components/CardElement.vue')),
    grid: defineAsyncComponent(() => import('./components/GridElement.vue')),
    divider: defineAsyncComponent(() => import('./components/DividerElement.vue')),
    space: defineAsyncComponent(() => import('./components/SpaceElement.vue')),
  };
  return componentMap[type] || componentMap.text;
}

// 拖拽从组件面板到画布
function onDragStart(event: DragEvent, component: any) {
  draggingComponent.value = component;
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'copy';
    event.dataTransfer.setData('application/json', JSON.stringify(component));
    event.dataTransfer.setData('text/plain', component.type);
  }
  
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

  // 获取鼠标位置相对于画布的坐标
  const canvasContent = document.querySelector('.canvas-content') as HTMLElement;
  let x = 20;
  let y = 20;
  
  if (event.dataTransfer && canvasContent) {
    const rect = canvasContent.getBoundingClientRect();
    x = Math.max(0, (event.dataTransfer as DataTransfer).getData('text/plain') ? 20 : (event.clientX - rect.left - 100));
    y = Math.max(0, event.clientY - rect.top - 50);
  }

  const newElement: PageElement = {
    id: `el_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    type: draggingComponent.value.type,
    name: `${draggingComponent.value.label}_${pageElements.value.length + 1}`,
    props: getDefaultProps(draggingComponent.value.type),
    x: Math.round(x),
    y: Math.round(y),
    width: 280,
    height: 80,
  };

  pageElements.value.push(newElement);
  selectedElementId.value = newElement.id;
  saveHistory();
  draggingComponent.value = null;

  MessagePlugin.success(`已添加 ${getComponentLabel(newElement.type)}`);
}

function quickAdd(type: string) {
  const newElement: PageElement = {
    id: `el_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    type: type,
    name: `${getComponentLabel(type)}_${pageElements.value.length + 1}`,
    props: getDefaultProps(type),
    x: 20 + (pageElements.value.length % 3) * 100,
    y: 20 + Math.floor(pageElements.value.length / 3) * 100,
    width: 280,
    height: 80,
  };

  pageElements.value.push(newElement);
  selectedElementId.value = newElement.id;
  saveHistory();

  MessagePlugin.success(`已添加 ${getComponentLabel(type)}`);
}

function getDefaultProps(type: string): Record<string, any> {
  const defaults: Record<string, Record<string, any>> = {
    text: { text: '双击编辑文本内容', fontSize: 16, color: '#1f2329', align: 'left' },
    input: { placeholder: '请输入内容', required: false, maxLength: 255, type: 'text' },
    button: { text: '点击按钮', theme: 'primary', size: 'medium', variant: 'base' },
    select: { placeholder: '请选择', options: [], multiple: false },
    date: { placeholder: '选择日期', range: false },
    checkbox: { label: '复选选项', checked: false },
    radio: { label: '单选选项', checked: false },
    table: { dataSource: '', border: true, showIndex: true, stripe: false, pagination: true },
    form: { labelWidth: 100 },
    list: { data: [] },
    chart: { type: 'bar', data: [] },
    card: { title: '卡片标题', border: true, hoverShadow: true, collapsible: false },
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
  MessagePlugin.success(`已复制 ${getComponentLabel(newElement.type)}`);
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
  const name = getComponentLabel(pageElements.value[index].type);
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

function handleClear() {
  if (pageElements.value.length === 0) {
    MessagePlugin.info('画布已经是空的');
    return;
  }
  
  if (confirm('确定要清空画布吗？此操作不可撤销。')) {
    pageElements.value = [];
    selectedElementId.value = null;
    saveHistory();
    MessagePlugin.success('画布已清空');
  }
}

function resetProps() {
  if (!selectedElement.value) return;
  const defaultProps = getDefaultProps(selectedElement.value.type);
  selectedElement.value.props = { ...defaultProps };
  MessagePlugin.success('属性已重置');
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
}

function handleRedo() {
  if (!canRedo.value) return;
  historyIndex.value++;
  pageElements.value = JSON.parse(JSON.stringify(history.value[historyIndex.value]));
}

// 自由拖拽函数
function onMouseDown(event: MouseEvent, element: PageElement) {
  const target = event.target as HTMLElement;
  // 如果点击的是拖拽手柄或调整大小手柄，不触发选择
  if (target.closest('.drag-handle') || target.closest('.element-actions')) {
    return;
  }
  
  draggingElementId.value = element.id;
  draggingElement.value = element;
  
  const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
  dragOffset.value = {
    x: event.clientX - rect.left,
    y: event.clientY - rect.top,
  };
  
  document.addEventListener('mousemove', onMouseMove);
  document.addEventListener('mouseup', onMouseUp);
}

function onMouseMove(event: MouseEvent) {
  if (!draggingElement.value) return;
  
  const canvasContent = document.querySelector('.canvas-content') as HTMLElement;
  if (!canvasContent) return;
  
  const rect = canvasContent.getBoundingClientRect();
  const newX = Math.max(0, event.clientX - rect.left - dragOffset.value.x);
  const newY = Math.max(0, event.clientY - rect.top - dragOffset.value.y);
  
  draggingElement.value.x = Math.round(newX);
  draggingElement.value.y = Math.round(newY);
}

function onMouseUp() {
  if (draggingElement.value) {
    saveHistory();
  }
  draggingElementId.value = null;
  draggingElement.value = null;
  
  document.removeEventListener('mousemove', onMouseMove);
  document.removeEventListener('mouseup', onMouseUp);
}

// 调整大小函数
function onResizeStart(event: MouseEvent, element: PageElement) {
  isResizing.value = true;
  draggingElement.value = element;
  resizeStartPos.value = { x: event.clientX, y: event.clientY };
  resizeStartSize.value = { width: element.width, height: element.height };
  
  document.addEventListener('mousemove', onResizeMove);
  document.addEventListener('mouseup', onResizeUp);
}

function onResizeMove(event: MouseEvent) {
  if (!isResizing.value || !draggingElement.value) return;
  
  const deltaX = event.clientX - resizeStartPos.value.x;
  const deltaY = event.clientY - resizeStartPos.value.y;
  
  draggingElement.value.width = Math.max(100, resizeStartSize.value.width + deltaX);
  draggingElement.value.height = Math.max(60, resizeStartSize.value.height + deltaY);
}

function onResizeUp() {
  if (draggingElement.value) {
    saveHistory();
  }
  isResizing.value = false;
  draggingElement.value = null;
  
  document.removeEventListener('mousemove', onResizeMove);
  document.removeEventListener('mouseup', onResizeUp);
}

function handlePreview() {
  currentView.value = 'preview';
  MessagePlugin.info('预览模式已开启');
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
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

/* 顶部工具栏 */
.designer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 100;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .back-btn {
      color: rgba(255, 255, 255, 0.85);
      &:hover {
        color: #fff;
        background: rgba(255, 255, 255, 0.1);
      }
    }

    .page-info {
      display: flex;
      align-items: center;
      gap: 10px;

      .page-name-input {
        font-size: 16px;
        font-weight: 600;
        color: #fff;
        border: none;
        outline: none;
        background: rgba(255, 255, 255, 0.1);
        padding: 8px 16px;
        border-radius: 8px;
        width: 220px;
        transition: all 0.2s;

        &:focus {
          background: rgba(255, 255, 255, 0.15);
          box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.5);
        }

        &::placeholder {
          color: rgba(255, 255, 255, 0.6);
        }
      }

      .page-status {
        font-size: 12px;
        color: #fbbf24;
        padding: 2px 8px;
        background: rgba(251, 191, 36, 0.15);
        border-radius: 10px;
        font-weight: 500;
      }
    }
  }

  .header-center {
    .view-tabs {
      display: flex;
      background: rgba(255, 255, 255, 0.08);
      border-radius: 8px;
      padding: 4px;

      .view-tab {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 20px;
        border-radius: 6px;
        color: rgba(255, 255, 255, 0.7);
        font-size: 13px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s;
        border: none;
        background: transparent;

        &:hover {
          background: rgba(255, 255, 255, 0.1);
          color: rgba(255, 255, 255, 0.9);
        }

        &.active {
          background: rgba(99, 102, 241, 0.9);
          color: #fff;
        }
      }
    }
  }

  .header-right {
    .toolbar-btn {
      background: rgba(255, 255, 255, 0.1);
      border-color: rgba(255, 255, 255, 0.2);
      color: rgba(255, 255, 255, 0.9);

      &:hover:not(:disabled) {
        background: rgba(255, 255, 255, 0.15);
        border-color: rgba(255, 255, 255, 0.3);
      }
    }

    .save-btn {
      background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
      border: none;
      padding: 0 24px;
      font-weight: 600;

      &:hover {
        background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
        box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
      }
    }
  }
}

/* 主体区域 */
.designer-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* 左侧组件面板 */
.component-panel {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.05);

  &.collapsed {
    width: 48px;

    .panel-header {
      span {
        display: none;
      }
    }

    .panel-content {
      .component-group {
        .group-header {
          span {
            display: none;
          }
        }
      }
    }
  }

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    font-size: 14px;
    font-weight: 600;
    color: #1f2937;
    border-bottom: 1px solid #e5e7eb;
    background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);

    .collapse-btn {
      color: #6b7280;
      &:hover {
        color: #374151;
        background: rgba(0, 0, 0, 0.05);
      }
    }
  }

  .panel-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px;

    &.collapsed {
      padding: 8px 4px;

      .component-item {
        justify-content: center;

        .component-label {
          display: none;
        }
      }
    }

    .component-group {
      margin-bottom: 16px;

      .group-header {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 10px;
        border-radius: 6px;
        cursor: pointer;
        transition: background 0.2s;
        color: #6b7280;
        font-size: 12px;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.5px;

        &:hover {
          background: #f3f4f6;
        }

        svg {
          flex-shrink: 0;
        }

        .rotated {
          transform: rotate(180deg);
        }
      }

      .group-items {
        padding: 8px 4px;
        animation: slideDown 0.2s ease;
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
        background: #f9fafb;

        &:hover {
          background: #eff6ff;
          border-color: #3b82f6;
          transform: translateX(4px);
          box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
        }

        &:active {
          cursor: grabbing;
          transform: scale(0.98);
        }

        &.dragging {
          opacity: 0.5;
          background: #dbeafe;
          border-color: #3b82f6;
        }

        .component-icon-wrapper {
          width: 36px;
          height: 36px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
          border-radius: 8px;
          color: #fff;
          flex-shrink: 0;
          transition: transform 0.2s;

          &:hover {
            transform: scale(1.1);
          }
        }

        .component-label {
          font-size: 13px;
          color: #374151;
          font-weight: 500;
        }
      }
    }
  }
}

/* 中间画布区域 */
.canvas-area {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  position: relative;
  transition: background 0.3s;

  &.drag-over {
    background: rgba(59, 130, 246, 0.08);

    .canvas-container {
      border-color: #3b82f6;
      box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.3);
    }
  }

  .canvas-container {
    max-width: 900px;
    margin: 0 auto;
    min-height: calc(100vh - 160px);
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
    border: 2px dashed #e5e7eb;
    transition: all 0.3s;
    overflow: hidden;
  }

  .canvas-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
    border-bottom: 1px solid #e5e7eb;

    .canvas-title {
      font-size: 14px;
      font-weight: 600;
      color: #374151;
    }

    .canvas-actions {
      :deep(.t-button) {
        color: #6b7280;
        &:hover {
          color: #ef4444;
        }
      }
    }
  }

  .canvas-content {
    padding: 20px;
    min-height: 400px;
    position: relative;
    background-image: 
      linear-gradient(to right, #e5e7eb 1px, transparent 1px),
      linear-gradient(to bottom, #e5e7eb 1px, transparent 1px);
    background-size: 20px 20px;
  }

  .canvas-element {
    position: absolute;
    cursor: move;
    border-radius: 12px;
    border: 2px solid #e5e7eb;
    background: #fff;
    transition: all 0.15s ease;
    overflow: hidden;
    z-index: 10;

    &:hover {
      border-color: #93c5fd;
      box-shadow: 0 4px 16px rgba(147, 197, 253, 0.3);
    }

    &.selected {
      border-color: #3b82f6;
      background: #f0f9ff;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
      z-index: 20;
    }

    &.is-dragging {
      opacity: 0.8;
      border-color: #3b82f6;
      box-shadow: 0 8px 24px rgba(59, 130, 246, 0.3);
      cursor: grabbing;
      z-index: 100;
    }

    &.drop-target {
      border-color: #3b82f6;
      border-style: dashed;
      background: rgba(59, 130, 246, 0.05);
      
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 2px;
        background: #3b82f6;
      }
    }

    .element-header {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      background: #f9fafb;
      border-bottom: 1px solid #e5e7eb;
      transition: opacity 0.2s;

      .drag-handle {
        cursor: grab;
        padding: 4px;
        border-radius: 4px;
        color: #9ca3af;
        transition: all 0.2s;

        &:hover {
          background: #e5e7eb;
          color: #374151;
        }

        &:active {
          cursor: grabbing;
        }
      }

      .element-type {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: #6b7280;
        font-weight: 500;
        flex: 1;
      }

      .element-actions {
        display: flex;
        align-items: center;
        gap: 2px;
        padding: 2px;
        background: #fff;
        border-radius: 6px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        border: 1px solid #e5e7eb;

        :deep(.t-button) {
          width: 26px;
          height: 26px;
          padding: 0;
          color: #6b7280;

          &:hover {
            color: #3b82f6;
            background: rgba(59, 130, 246, 0.1);
          }

          &.t-button--theme-danger:hover {
            color: #ef4444;
            background: rgba(239, 68, 68, 0.1);
          }

          &:disabled {
            opacity: 0.4;
            cursor: not-allowed;
          }
        }
      }
    }

    .element-content {
      padding: 16px;
    }

    .element-resize-handle {
      position: absolute;
      bottom: 0;
      right: 0;
      width: 12px;
      height: 12px;
      cursor: se-resize;
      border-right: 2px solid #9ca3af;
      border-bottom: 2px solid #9ca3af;
      opacity: 0;
      transition: opacity 0.2s;

      &:hover {
        border-color: #3b82f6;
      }
    }

    &:hover .element-resize-handle {
      opacity: 1;
    }
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
    padding: 40px 60px;
    background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
    color: #fff;
    border-radius: 20px;
    font-size: 18px;
    font-weight: 600;
    box-shadow: 0 12px 48px rgba(59, 130, 246, 0.4);
    z-index: 1000;
    pointer-events: none;
    animation: pulse 1.5s ease-in-out infinite;

    .hint-icon {
      width: 64px;
      height: 64px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
    }

    .hint-shortcut {
      font-size: 13px;
      opacity: 0.8;
      margin-top: 4px;
    }
  }

  .empty-canvas {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 100px 20px;
    color: #6b7280;

    .empty-illustration {
      position: relative;
      margin-bottom: 24px;

      .illustration-bg {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 140px;
        height: 140px;
        background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
        border-radius: 50%;
        opacity: 0.1;
        animation: expand 3s ease-in-out infinite;
      }

      .illustration-icon {
        position: relative;
        width: 100px;
        height: 100px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #eff6ff 0%, #e0e7ff 100%);
        border-radius: 24px;
        color: #6366f1;
        box-shadow: 0 8px 32px rgba(99, 102, 241, 0.15);
      }
    }

    h3 {
      font-size: 20px;
      font-weight: 600;
      color: #1f2937;
      margin: 0 0 8px 0;
    }

    p {
      margin: 0 0 28px 0;
      font-size: 14px;
      text-align: center;
      color: #9ca3af;
    }

    .quick-add-buttons {
      display: flex;
      gap: 12px;

      .quick-btn {
        background: #fff;
        border-color: #e5e7eb;
        color: #374151;

        &:hover {
          background: #f9fafb;
          border-color: #3b82f6;
          color: #3b82f6;
        }
      }
    }
  }
}

/* 右侧属性面板 */
.property-panel {
  width: 320px;
  background: #fff;
  border-left: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.05);

  &.collapsed {
    width: 48px;

    .panel-header {
      span {
        display: none;
      }
    }

    .panel-content {
      display: none;
    }
  }

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    font-size: 14px;
    font-weight: 600;
    color: #1f2937;
    border-bottom: 1px solid #e5e7eb;
    background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);

    .collapse-btn {
      color: #6b7280;
      &:hover {
        color: #374151;
        background: rgba(0, 0, 0, 0.05);
      }
    }
  }

  .panel-content {
    flex: 1;
    overflow-y: auto;
    padding: 16px;

    &.collapsed {
      display: none;
    }

    .property-form {
      .property-section {
        margin-bottom: 20px;

        .section-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;

          .section-title {
            font-size: 12px;
            font-weight: 600;
            color: #6b7280;
            text-transform: uppercase;
            letter-spacing: 0.5px;
          }

          .reset-btn {
            font-size: 12px;
            color: #6b7280;

            &:hover {
              color: #3b82f6;
            }
          }
        }

        .section-content {
          .id-input {
            background: #f3f4f6;
            color: #9ca3af;
          }

          .type-tag {
            font-size: 12px;
          }

          .slider-control {
            display: flex;
            align-items: center;
            gap: 12px;

            .slider-value {
              font-size: 13px;
              color: #374151;
              font-weight: 500;
              min-width: 60px;
            }
          }

          .color-picker-wrapper {
            display: flex;
            gap: 8px;

            .color-input {
              width: 100px;
            }
          }

          .no-properties-tip {
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 30px 20px;
            color: #9ca3af;

            p {
              margin: 12px 0 0 0;
              font-size: 13px;
            }
          }
        }
      }

      .property-actions {
        margin-top: 8px;
        padding-top: 16px;
        border-top: 1px solid #e5e7eb;
      }
    }

    .empty-property {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
      padding: 80px 20px;
      color: #9ca3af;

      .empty-icon {
        width: 80px;
        height: 80px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
        border-radius: 50%;
        color: #9ca3af;
        margin-bottom: 16px;
      }

      h4 {
        font-size: 16px;
        font-weight: 600;
        color: #374151;
        margin: 0 0 8px 0;
      }

      p {
        font-size: 13px;
        line-height: 1.6;
        margin: 0;
      }
    }
  }
}

/* 动画 */
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

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes expand {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.1;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.3);
    opacity: 0.05;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 调整大小手柄 */
.element-resize-handle {
  position: absolute;
  right: 4px;
  bottom: 4px;
  width: 12px;
  height: 12px;
  background: #3b82f6;
  border-radius: 50%;
  cursor: se-resize;
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.4);
  transition: all 0.2s;

  &:hover {
    background: #2563eb;
    transform: scale(1.2);
  }
}

/* 预览视图 */
.preview-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;

  .preview-container {
    width: 100%;
    max-width: 1200px;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
    overflow: hidden;
  }

  .preview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
    color: #fff;

    .preview-title {
      font-size: 18px;
      font-weight: 600;
    }
  }

  .preview-content {
    padding: 20px;
    min-height: 500px;
    position: relative;
    background: #f9fafb;
  }

  .preview-element {
    position: absolute;
    background: #fff;
    padding: 16px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  .empty-preview {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 300px;
    color: #9ca3af;
    text-align: center;

    p {
      margin-top: 16px;
      font-size: 14px;
    }
  }
}
</style>