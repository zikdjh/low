<template>
  <div class="page-designer">
    <!-- 顶部工具栏 -->
    <div class="designer-header">
      <div class="header-left">
        <BackButton to="/lowcode/page/list" label="返回页面列表" class="designer-back-btn" />
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
                <t-button size="small" variant="outline" @click="quickAdd('container')" class="quick-btn">
                  <FolderIcon /> 添加容器
                </t-button>
                <t-button size="small" variant="outline" @click="quickAdd('card')" class="quick-btn">
                  <RectangleIcon /> 添加卡片
                </t-button>
                <t-button size="small" variant="outline" @click="quickAdd('tabs')" class="quick-btn">
                  <FrameIcon /> 标签页
                </t-button>
                <t-button size="small" variant="outline" @click="quickAdd('text')" class="quick-btn">
                  <EditIcon /> 添加文本
                </t-button>
                <t-button size="small" variant="outline" @click="quickAdd('table')" class="quick-btn">
                  <TableIcon /> 数据表格
                </t-button>
                <t-button size="small" variant="outline" @click="quickAdd('button')" class="quick-btn">
                  <ButtonIcon /> 添加按钮
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
                          :min="12" :max="72" :show-tooltip="true"
                          :marks="{ 12: '12px', 24: '24px', 36: '36px', 48: '48px', 72: '72px' }"
                        />
                        <span class="slider-value">{{ selectedElement.props.fontSize }}px</span>
                      </div>
                    </t-form-item>
                    <t-form-item label="字体颜色">
                      <div class="color-picker-wrapper">
                        <t-color-picker v-model="selectedElement.props.color" format="HEX" />
                        <t-input v-model="selectedElement.props.color" size="small" class="color-input" />
                      </div>
                    </t-form-item>
                    <t-form-item label="对齐方式">
                      <t-radio-group v-model="selectedElement.props.align" variant="default-filled" size="small">
                        <t-radio-button value="left">左对齐</t-radio-button>
                        <t-radio-button value="center">居中</t-radio-button>
                        <t-radio-button value="right">右对齐</t-radio-button>
                      </t-radio-group>
                    </t-form-item>
                  </template>

                  <!-- 链接组件属性 -->
                  <template v-else-if="selectedElement.type === 'link'">
                    <t-form-item label="链接文字">
                      <t-input v-model="selectedElement.props.text" placeholder="链接文字" />
                    </t-form-item>
                    <t-form-item label="链接地址">
                      <t-input v-model="selectedElement.props.href" placeholder="https://" />
                    </t-form-item>
                    <t-form-item label="链接主题">
                      <t-select v-model="selectedElement.props.theme">
                        <t-option value="default" label="默认" />
                        <t-option value="primary" label="主色" />
                        <t-option value="success" label="成功" />
                        <t-option value="warning" label="警告" />
                        <t-option value="danger" label="危险" />
                      </t-select>
                    </t-form-item>
                    <t-form-item label="下划线">
                      <t-switch v-model="selectedElement.props.underline" />
                    </t-form-item>
                    <t-form-item label="打开方式">
                      <t-select v-model="selectedElement.props.target">
                        <t-option value="_self" label="当前窗口" />
                        <t-option value="_blank" label="新窗口" />
                      </t-select>
                    </t-form-item>
                  </template>

                  <!-- 图片组件属性 -->
                  <template v-else-if="selectedElement.type === 'image'">
                    <t-form-item label="图片地址">
                      <t-input v-model="selectedElement.props.src" placeholder="输入图片URL" />
                    </t-form-item>
                    <t-form-item label="替代文本">
                      <t-input v-model="selectedElement.props.alt" placeholder="图片描述" />
                    </t-form-item>
                    <t-form-item label="图片宽度">
                      <t-input v-model="selectedElement.props.width" placeholder="如 100%/200px" />
                    </t-form-item>
                    <t-form-item label="图片高度">
                      <t-input v-model="selectedElement.props.height" placeholder="如 auto/150px" />
                    </t-form-item>
                    <t-form-item label="圆角">
                      <t-input-number v-model.number="selectedElement.props.radius" :min="0" :max="50" />
                    </t-form-item>
                    <t-form-item label="对齐">
                      <t-select v-model="selectedElement.props.align">
                        <t-option value="left" label="左对齐" />
                        <t-option value="center" label="居中" />
                        <t-option value="right" label="右对齐" />
                      </t-select>
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

                  <!-- 文本域属性 -->
                  <template v-else-if="selectedElement.type === 'textarea'">
                    <t-form-item label="占位文本">
                      <t-input v-model="selectedElement.props.placeholder" placeholder="输入占位文本" />
                    </t-form-item>
                    <t-form-item label="最小行数">
                      <t-input-number v-model.number="selectedElement.props.minRows" :min="1" :max="20" />
                    </t-form-item>
                    <t-form-item label="最大行数">
                      <t-input-number v-model.number="selectedElement.props.maxRows" :min="1" :max="50" />
                    </t-form-item>
                    <t-form-item label="最大长度">
                      <t-input-number v-model="selectedElement.props.maxlength" :min="0" :max="5000" />
                    </t-form-item>
                    <t-form-item label="自适应高度">
                      <t-switch v-model="selectedElement.props.autosize" />
                    </t-form-item>
                  </template>

                  <!-- 数字输入属性 -->
                  <template v-else-if="selectedElement.type === 'inputNumber'">
                    <t-form-item label="默认值">
                      <t-input-number v-model.number="selectedElement.props.value" />
                    </t-form-item>
                    <t-form-item label="最小值">
                      <t-input-number v-model="selectedElement.props.min" />
                    </t-form-item>
                    <t-form-item label="最大值">
                      <t-input-number v-model="selectedElement.props.max" />
                    </t-form-item>
                    <t-form-item label="步长">
                      <t-input-number v-model.number="selectedElement.props.step" :min="0.1" />
                    </t-form-item>
                    <t-form-item label="占位文本">
                      <t-input v-model="selectedElement.props.placeholder" />
                    </t-form-item>
                    <t-form-item label="主题">
                      <t-select v-model="selectedElement.props.theme">
                        <t-option value="normal" label="普通" />
                        <t-option value="column" label="竖排" />
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

                  <!-- 时间选择属性 -->
                  <template v-else-if="selectedElement.type === 'time'">
                    <t-form-item label="占位文本">
                      <t-input v-model="selectedElement.props.placeholder" placeholder="选择时间" />
                    </t-form-item>
                    <t-form-item label="时间格式">
                      <t-select v-model="selectedElement.props.format">
                        <t-option value="HH:mm:ss" label="HH:mm:ss" />
                        <t-option value="HH:mm" label="HH:mm" />
                        <t-option value="hh:mm:ss" label="hh:mm:ss" />
                      </t-select>
                    </t-form-item>
                    <t-form-item label="可清除">
                      <t-switch v-model="selectedElement.props.clearable" />
                    </t-form-item>
                  </template>

                  <!-- 开关属性 -->
                  <template v-else-if="selectedElement.type === 'switch'">
                    <t-form-item label="前置标签">
                      <t-input v-model="selectedElement.props.labelBefore" placeholder="开关前的文字" />
                    </t-form-item>
                    <t-form-item label="后置标签">
                      <t-input v-model="selectedElement.props.labelAfter" placeholder="开关后的文字" />
                    </t-form-item>
                    <t-form-item label="默认开启">
                      <t-switch v-model="selectedElement.props.checked" />
                    </t-form-item>
                    <t-form-item label="尺寸">
                      <t-select v-model="selectedElement.props.size">
                        <t-option value="small" label="小" />
                        <t-option value="medium" label="中" />
                        <t-option value="large" label="大" />
                      </t-select>
                    </t-form-item>
                  </template>

                  <!-- 滑块属性 -->
                  <template v-else-if="selectedElement.type === 'slider'">
                    <t-form-item label="标签">
                      <t-input v-model="selectedElement.props.label" placeholder="滑块标签" />
                    </t-form-item>
                    <t-form-item label="默认值">
                      <t-input-number v-model.number="selectedElement.props.value" :min="0" :max="100" />
                    </t-form-item>
                    <t-form-item label="最小值">
                      <t-input-number v-model.number="selectedElement.props.min" />
                    </t-form-item>
                    <t-form-item label="最大值">
                      <t-input-number v-model.number="selectedElement.props.max" />
                    </t-form-item>
                    <t-form-item label="步长">
                      <t-input-number v-model.number="selectedElement.props.step" :min="0.1" />
                    </t-form-item>
                    <t-form-item label="显示标签">
                      <t-switch v-model="selectedElement.props.showLabel" />
                    </t-form-item>
                    <t-form-item label="显示提示">
                      <t-switch v-model="selectedElement.props.showTooltip" />
                    </t-form-item>
                  </template>

                  <!-- 评分属性 -->
                  <template v-else-if="selectedElement.type === 'rate'">
                    <t-form-item label="标签">
                      <t-input v-model="selectedElement.props.label" placeholder="评分标签" />
                    </t-form-item>
                    <t-form-item label="默认值">
                      <t-input-number v-model.number="selectedElement.props.value" :min="1" :max="10" />
                    </t-form-item>
                    <t-form-item label="星星数量">
                      <t-input-number v-model.number="selectedElement.props.count" :min="3" :max="10" />
                    </t-form-item>
                    <t-form-item label="允许半星">
                      <t-switch v-model="selectedElement.props.allowHalf" />
                    </t-form-item>
                    <t-form-item label="只读">
                      <t-switch v-model="selectedElement.props.readonly" />
                    </t-form-item>
                  </template>

                  <!-- 上传属性 -->
                  <template v-else-if="selectedElement.type === 'upload'">
                    <t-form-item label="提示文字">
                      <t-input v-model="selectedElement.props.hint" placeholder="上传提示" />
                    </t-form-item>
                    <t-form-item label="最大数量">
                      <t-input-number v-model.number="selectedElement.props.max" :min="1" :max="100" />
                    </t-form-item>
                    <t-form-item label="文件大小(MB)">
                      <t-input-number v-model.number="selectedElement.props.maxSize" :min="1" :max="500" />
                    </t-form-item>
                    <t-form-item label="多文件上传">
                      <t-switch v-model="selectedElement.props.multiple" />
                    </t-form-item>
                    <t-form-item label="可拖拽">
                      <t-switch v-model="selectedElement.props.draggable" />
                    </t-form-item>
                  </template>

                  <!-- 标签属性 -->
                  <template v-else-if="selectedElement.type === 'tag'">
                    <t-form-item label="标签内容">
                      <t-textarea 
                        v-model="selectedElement.props.tags" 
                        placeholder="用逗号分隔多个标签"
                        :autosize="{ minRows: 2, maxRows: 4 }"
                      />
                    </t-form-item>
                    <t-form-item label="标签主题">
                      <t-select v-model="selectedElement.props.theme">
                        <t-option value="primary" label="主色" />
                        <t-option value="success" label="成功" />
                        <t-option value="warning" label="警告" />
                        <t-option value="danger" label="危险" />
                      </t-select>
                    </t-form-item>
                    <t-form-item label="标签变体">
                      <t-select v-model="selectedElement.props.variant">
                        <t-option value="light" label="浅色" />
                        <t-option value="dark" label="深色" />
                        <t-option value="outline" label="描边" />
                      </t-select>
                    </t-form-item>
                    <t-form-item label="尺寸">
                      <t-select v-model="selectedElement.props.size">
                        <t-option value="small" label="小" />
                        <t-option value="medium" label="中" />
                        <t-option value="large" label="大" />
                      </t-select>
                    </t-form-item>
                  </template>

                  <!-- 进度条属性 -->
                  <template v-else-if="selectedElement.type === 'progress'">
                    <t-form-item label="标题">
                      <t-input v-model="selectedElement.props.label" placeholder="进度标题" />
                    </t-form-item>
                    <t-form-item label="百分比">
                      <t-input-number v-model.number="selectedElement.props.percent" :min="0" :max="100" />
                    </t-form-item>
                    <t-form-item label="线条宽度">
                      <t-input-number v-model.number="selectedElement.props.strokeWidth" :min="2" :max="50" />
                    </t-form-item>
                    <t-form-item label="显示标题">
                      <t-switch v-model="selectedElement.props.showLabel" />
                    </t-form-item>
                    <t-form-item label="显示百分比">
                      <t-switch v-model="selectedElement.props.showPercent" />
                    </t-form-item>
                  </template>

                  <!-- 步骤条属性 -->
                  <template v-else-if="selectedElement.type === 'steps'">
                    <t-form-item label="当前步骤">
                      <t-input-number v-model.number="selectedElement.props.current" :min="0" :max="10" />
                    </t-form-item>
                    <t-form-item label="布局方向">
                      <t-select v-model="selectedElement.props.layout">
                        <t-option value="horizontal" label="水平" />
                        <t-option value="vertical" label="垂直" />
                      </t-select>
                    </t-form-item>
                    <t-form-item label="主题">
                      <t-select v-model="selectedElement.props.theme">
                        <t-option value="default" label="默认" />
                        <t-option value="dot" label="圆点" />
                      </t-select>
                    </t-form-item>
                  </template>

                  <!-- 提示属性 -->
                  <template v-else-if="selectedElement.type === 'alert'">
                    <t-form-item label="标题">
                      <t-input v-model="selectedElement.props.title" placeholder="提示标题" />
                    </t-form-item>
                    <t-form-item label="内容">
                      <t-textarea v-model="selectedElement.props.message" placeholder="提示内容" :autosize="{ minRows: 2, maxRows: 4 }" />
                    </t-form-item>
                    <t-form-item label="主题">
                      <t-select v-model="selectedElement.props.theme">
                        <t-option value="info" label="信息" />
                        <t-option value="success" label="成功" />
                        <t-option value="warning" label="警告" />
                        <t-option value="error" label="错误" />
                      </t-select>
                    </t-form-item>
                    <t-form-item label="可关闭">
                      <t-switch v-model="selectedElement.props.closable" />
                    </t-form-item>
                  </template>

                  <!-- 容器属性 -->
                  <template v-else-if="selectedElement.type === 'container'">
                    <t-form-item label="容器标题">
                      <t-input v-model="selectedElement.props.title" placeholder="容器标题" />
                    </t-form-item>
                    <t-form-item label="内边距">
                      <t-input-number v-model.number="selectedElement.props.padding" :min="0" :max="60" />
                    </t-form-item>
                    <t-form-item label="圆角">
                      <t-input-number v-model.number="selectedElement.props.borderRadius" :min="0" :max="30" />
                    </t-form-item>
                    <t-form-item label="背景色">
                      <div class="color-picker-wrapper">
                        <t-color-picker v-model="selectedElement.props.bgColor" format="HEX" />
                        <t-input v-model="selectedElement.props.bgColor" size="small" class="color-input" />
                      </div>
                    </t-form-item>
                    <t-form-item label="阴影">
                      <t-switch v-model="selectedElement.props.shadow" />
                    </t-form-item>
                    <t-form-item label="显示页脚">
                      <t-switch v-model="selectedElement.props.showFooter" />
                    </t-form-item>
                    <t-form-item v-if="selectedElement.props.showFooter" label="页脚文字">
                      <t-input v-model="selectedElement.props.footerText" />
                    </t-form-item>
                  </template>

                  <!-- 标签页属性 -->
                  <template v-else-if="selectedElement.type === 'tabs'">
                    <t-form-item label="标签内容">
                      <t-textarea 
                        v-model="selectedElement.props.tabs" 
                        placeholder="用逗号分隔，如：标签1,标签2,标签3"
                        :autosize="{ minRows: 2, maxRows: 4 }"
                      />
                    </t-form-item>
                    <t-form-item label="尺寸">
                      <t-select v-model="selectedElement.props.size">
                        <t-option value="small" label="小" />
                        <t-option value="medium" label="中" />
                        <t-option value="large" label="大" />
                      </t-select>
                    </t-form-item>
                    <t-form-item label="主题">
                      <t-select v-model="selectedElement.props.theme">
                        <t-option value="normal" label="普通" />
                        <t-option value="card" label="卡片" />
                      </t-select>
                    </t-form-item>
                    <t-form-item label="位置">
                      <t-select v-model="selectedElement.props.placement">
                        <t-option value="top" label="上" />
                        <t-option value="bottom" label="下" />
                        <t-option value="left" label="左" />
                        <t-option value="right" label="右" />
                      </t-select>
                    </t-form-item>
                  </template>

                  <!-- 折叠面板属性 -->
                  <template v-else-if="selectedElement.type === 'collapse'">
                    <t-form-item label="手风琴模式">
                      <t-switch v-model="selectedElement.props.accordion" />
                    </t-form-item>
                    <t-form-item label="无边框">
                      <t-switch v-model="selectedElement.props.borderless" />
                    </t-form-item>
                    <t-form-item label="全部展开">
                      <t-switch v-model="selectedElement.props.expandAll" />
                    </t-form-item>
                  </template>

                  <!-- 面包屑属性 -->
                  <template v-else-if="selectedElement.type === 'breadcrumb'">
                    <t-form-item label="最大项数">
                      <t-input-number v-model.number="selectedElement.props.maxItems" :min="0" :max="10" />
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
import { ref, computed, markRaw, defineAsyncComponent, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import { pageSchemaApi } from '../../../api/lowcode/pageSchema';
import BackButton from '../../../components/common/BackButton.vue';
import {
  ChevronLeftIcon, ChevronRightIcon, ChevronUpIcon, ChevronDownIcon,
  RollbackIcon, ForwardIcon, BrowseIcon, SaveIcon,
  AppIcon, EditIcon, ButtonIcon, CalendarIcon,
  CheckCircleFilledIcon, CircleIcon, TableIcon, FormIcon, ViewListIcon,
  ChartBarIcon, LayoutIcon, MinusIcon, ExpandHorizontalIcon,
  VerticalIcon, CopyIcon, DeleteIcon, LinkIcon,
  SettingIcon, DownloadIcon, RectangleIcon,
  InkIcon, TapeIcon, ConstraintIcon, CursorIcon,
  ImageIcon, TimeIcon, AddCircleIcon, SwapIcon, TextboxIcon,
  LayersIcon, CodeIcon, FrameIcon, FolderIcon, ControlPlatformIcon,
  UploadIcon, StarFilledIcon, TipsIcon, RootListIcon,
} from 'tdesign-icons-vue-next';
// 使用实际存在的图标作为兼容别名
const MousePointerIcon = CursorIcon;

const route = useRoute();

// 视图切换
const currentView = ref('design');
const viewTabs = [
  { key: 'design', label: '设计', icon: LayoutIcon },
  { key: 'preview', label: '预览', icon: BrowseIcon },
];

// 面板折叠状态
const panelCollapsed = ref(false);
const propertyPanelCollapsed = ref(false);
const expandedGroups = ref(['basic', 'layout']);

// 拖拽状态
const draggingComponent = ref<any>(null);
const isDraggingOver = ref(false);

// 页面状态
const pageName = ref('未命名页面');
// 页面ID（编辑模式）
const editingPageId = ref<number | null>(null);
const pageStatus = ref('draft');
const isSaving = ref(false);

// 从路由参数加载页面
onMounted(async () => {
  const id = route.query.id;
  if (id) {
    try {
      const res = await pageSchemaApi.getById(Number(id));
      if (res.data.code === 1) {
        const pageData = res.data.data;
        editingPageId.value = pageData.id ?? null;
        pageName.value = pageData.name || '未命名页面';
        pageStatus.value = pageData.status || 'draft';
        // 加载页面元素
        try {
          const layoutJson = typeof pageData.layoutJson === 'string'
            ? JSON.parse(pageData.layoutJson)
            : (pageData.layoutJson || []);
          pageElements.value = Array.isArray(layoutJson) ? layoutJson : [];
        } catch { /* 忽略解析错误 */ }
      }
    } catch (e) {
      MessagePlugin.warning('无法加载页面数据，将以新建模式打开');
    }
  }
});

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
      { type: 'button', label: '按钮', iconComponent: markRaw(ButtonIcon) },
      { type: 'link', label: '链接', iconComponent: markRaw(LinkIcon) },
      { type: 'image', label: '图片', iconComponent: markRaw(ImageIcon) },
    ]
  },
  {
    name: 'form',
    label: '表单组件',
    icon: FormIcon,
    components: [
      { type: 'input', label: '输入框', iconComponent: markRaw(TapeIcon) },
      { type: 'textarea', label: '文本域', iconComponent: markRaw(TextboxIcon) },
      { type: 'inputNumber', label: '数字输入', iconComponent: markRaw(SwapIcon) },
      { type: 'select', label: '下拉框', iconComponent: markRaw(ChevronDownIcon) },
      { type: 'date', label: '日期选择', iconComponent: markRaw(CalendarIcon) },
      { type: 'time', label: '时间选择', iconComponent: markRaw(TimeIcon) },
      { type: 'switch', label: '开关', iconComponent: markRaw(ControlPlatformIcon) },
      { type: 'checkbox', label: '复选框', iconComponent: markRaw(CheckCircleFilledIcon) },
      { type: 'radio', label: '单选框', iconComponent: markRaw(CircleIcon) },
      { type: 'slider', label: '滑块', iconComponent: markRaw(AddCircleIcon) },
      { type: 'rate', label: '评分', iconComponent: markRaw(StarFilledIcon) },
      { type: 'upload', label: '上传', iconComponent: markRaw(UploadIcon) },
    ]
  },
  {
    name: 'data',
    label: '数据组件',
    icon: TableIcon,
    components: [
      { type: 'table', label: '数据表格', iconComponent: markRaw(TableIcon) },
      { type: 'form', label: '表单容器', iconComponent: markRaw(FormIcon) },
      { type: 'list', label: '列表', iconComponent: markRaw(ViewListIcon) },
      { type: 'chart', label: '图表', iconComponent: markRaw(ChartBarIcon) },
    ]
  },
  {
    name: 'display',
    label: '展示组件',
    icon: LayoutIcon,
    components: [
      { type: 'card', label: '卡片', iconComponent: markRaw(LayoutIcon) },
      { type: 'tag', label: '标签', iconComponent: markRaw(CodeIcon) },
      { type: 'progress', label: '进度条', iconComponent: markRaw(SwapIcon) },
      { type: 'steps', label: '步骤条', iconComponent: markRaw(RootListIcon) },
      { type: 'alert', label: '提示信息', iconComponent: markRaw(TipsIcon) },
      { type: 'divider', label: '分割线', iconComponent: markRaw(MinusIcon) },
    ]
  },
  {
    name: 'layout',
    label: '布局组件',
    icon: ConstraintIcon,
    components: [
      { type: 'container', label: '容器', iconComponent: markRaw(FolderIcon) },
      { type: 'grid', label: '栅格', iconComponent: markRaw(LayersIcon) },
      { type: 'tabs', label: '标签页', iconComponent: markRaw(FrameIcon) },
      { type: 'collapse', label: '折叠面板', iconComponent: markRaw(ExpandHorizontalIcon) },
      { type: 'space', label: '间距', iconComponent: markRaw(ExpandHorizontalIcon) },
      { type: 'breadcrumb', label: '面包屑', iconComponent: markRaw(ChevronRightIcon) },
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
    // 基础组件
    text: defineAsyncComponent(() => import('./components/TextElement.vue')),
    button: defineAsyncComponent(() => import('./components/ButtonElement.vue')),
    link: defineAsyncComponent(() => import('./components/LinkElement.vue')),
    image: defineAsyncComponent(() => import('./components/ImageElement.vue')),
    // 表单组件
    input: defineAsyncComponent(() => import('./components/InputElement.vue')),
    textarea: defineAsyncComponent(() => import('./components/TextareaElement.vue')),
    inputNumber: defineAsyncComponent(() => import('./components/InputNumberElement.vue')),
    select: defineAsyncComponent(() => import('./components/SelectElement.vue')),
    date: defineAsyncComponent(() => import('./components/DateElement.vue')),
    time: defineAsyncComponent(() => import('./components/TimePickerElement.vue')),
    switch: defineAsyncComponent(() => import('./components/SwitchElement.vue')),
    checkbox: defineAsyncComponent(() => import('./components/CheckboxElement.vue')),
    radio: defineAsyncComponent(() => import('./components/RadioElement.vue')),
    slider: defineAsyncComponent(() => import('./components/SliderElement.vue')),
    rate: defineAsyncComponent(() => import('./components/RateElement.vue')),
    upload: defineAsyncComponent(() => import('./components/UploadElement.vue')),
    // 数据组件
    table: defineAsyncComponent(() => import('./components/TableElement.vue')),
    form: defineAsyncComponent(() => import('./components/FormElement.vue')),
    list: defineAsyncComponent(() => import('./components/ListElement.vue')),
    chart: defineAsyncComponent(() => import('./components/ChartElement.vue')),
    // 展示组件
    card: defineAsyncComponent(() => import('./components/CardElement.vue')),
    tag: defineAsyncComponent(() => import('./components/TagElement.vue')),
    progress: defineAsyncComponent(() => import('./components/ProgressElement.vue')),
    steps: defineAsyncComponent(() => import('./components/StepsElement.vue')),
    alert: defineAsyncComponent(() => import('./components/AlertElement.vue')),
    divider: defineAsyncComponent(() => import('./components/DividerElement.vue')),
    // 布局组件
    container: defineAsyncComponent(() => import('./components/ContainerElement.vue')),
    grid: defineAsyncComponent(() => import('./components/GridElement.vue')),
    tabs: defineAsyncComponent(() => import('./components/TabsElement.vue')),
    collapse: defineAsyncComponent(() => import('./components/CollapseElement.vue')),
    space: defineAsyncComponent(() => import('./components/SpaceElement.vue')),
    breadcrumb: defineAsyncComponent(() => import('./components/BreadcrumbElement.vue')),
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

  const size = getDefaultSize(draggingComponent.value.type);
  const newElement: PageElement = {
    id: `el_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    type: draggingComponent.value.type,
    name: `${draggingComponent.value.label}_${pageElements.value.length + 1}`,
    props: getDefaultProps(draggingComponent.value.type),
    x: Math.round(x),
    y: Math.round(y),
    width: size.width,
    height: size.height,
  };

  pageElements.value.push(newElement);
  selectedElementId.value = newElement.id;
  saveHistory();
  draggingComponent.value = null;

  MessagePlugin.success(`已添加 ${getComponentLabel(newElement.type)}`);
}

function quickAdd(type: string) {
  const size = getDefaultSize(type);
  const newElement: PageElement = {
    id: `el_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    type: type,
    name: `${getComponentLabel(type)}_${pageElements.value.length + 1}`,
    props: getDefaultProps(type),
    x: 20 + (pageElements.value.length % 2) * 340,
    y: 20 + Math.floor(pageElements.value.length / 2) * 220,
    width: size.width,
    height: size.height,
  };

  pageElements.value.push(newElement);
  selectedElementId.value = newElement.id;
  saveHistory();

  MessagePlugin.success(`已添加 ${getComponentLabel(type)}`);
}

function getDefaultSize(type: string): { width: number; height: number } {
  const sizes: Record<string, { width: number; height: number }> = {
    // 布局组件需要大空间
    container: { width: 360, height: 200 },
    tabs: { width: 360, height: 140 },
    collapse: { width: 320, height: 120 },
    grid: { width: 360, height: 100 },
    // 数据组件
    table: { width: 420, height: 180 },
    form: { width: 320, height: 160 },
    chart: { width: 360, height: 200 },
    steps: { width: 380, height: 90 },
    // 展示组件
    card: { width: 300, height: 120 },
    progress: { width: 240, height: 70 },
    alert: { width: 320, height: 70 },
    // 表单组件
    textarea: { width: 280, height: 110 },
    upload: { width: 280, height: 130 },
    image: { width: 240, height: 160 },
    slider: { width: 240, height: 70 },
    rate: { width: 200, height: 60 },
    // 默认
    text: { width: 200, height: 50 },
    button: { width: 120, height: 44 },
    switch: { width: 140, height: 44 },
    tag: { width: 280, height: 44 },
    link: { width: 120, height: 44 },
    breadcrumb: { width: 320, height: 44 },
    divider: { width: 360, height: 30 },
    space: { width: 200, height: 40 },
    select: { width: 220, height: 44 },
    date: { width: 220, height: 44 },
    time: { width: 180, height: 44 },
    checkbox: { width: 140, height: 40 },
    radio: { width: 140, height: 40 },
    input: { width: 280, height: 44 },
    inputNumber: { width: 180, height: 44 },
    list: { width: 320, height: 120 },
  };
  return sizes[type] || { width: 280, height: 80 };
}

function getDefaultProps(type: string): Record<string, any> {
  const defaults: Record<string, Record<string, any>> = {
    // 基础组件
    text: { text: '双击编辑文本内容', fontSize: 16, color: '#1f2329', align: 'left' },
    button: { text: '点击按钮', theme: 'primary', size: 'medium', variant: 'base' },
    link: { text: '链接文字', href: 'https://', theme: 'primary', underline: false, target: '_self' },
    image: { src: '', alt: '图片', width: '100%', height: 'auto', fit: 'cover', radius: 8, align: 'center' },
    // 表单组件
    input: { placeholder: '请输入内容', required: false, maxLength: 255, type: 'text' },
    textarea: { text: '', placeholder: '请输入多行文本', minRows: 3, maxRows: 6, maxlength: 500, autosize: true },
    inputNumber: { value: 0, min: undefined, max: undefined, step: 1, placeholder: '请输入数字', theme: 'normal', size: 'medium' },
    select: { placeholder: '请选择', options: [], multiple: false },
    date: { placeholder: '选择日期', range: false },
    time: { placeholder: '选择时间', format: 'HH:mm:ss', clearable: true, size: 'medium' },
    switch: { checked: false, labelBefore: '', labelAfter: '', size: 'medium' },
    checkbox: { label: '复选选项', checked: false },
    radio: { label: '单选选项', checked: false },
    slider: { value: 50, min: 0, max: 100, step: 1, showLabel: true, showTooltip: true, label: '滑块' },
    rate: { value: 3, count: 5, size: 20, allowHalf: false, readonly: false, color: '#f5a623', showLabel: true, label: '评分' },
    upload: { disabled: false, multiple: false, max: 5, draggable: true, hint: '点击或拖拽上传文件', maxSize: 10 },
    // 数据组件
    table: { dataSource: '', border: true, showIndex: true, stripe: false, pagination: true },
    form: { labelWidth: 100 },
    list: { data: [] },
    chart: { type: 'bar', data: [] },
    // 展示组件
    card: { title: '卡片标题', border: true, hoverShadow: true, collapsible: false },
    tag: { tags: ['默认', '成功', '警告', '危险', '信息'], theme: 'primary', variant: 'light', size: 'medium', gap: 8 },
    progress: { percent: 60, strokeWidth: 8, showLabel: true, showPercent: true, label: '进度', theme: 'default' },
    steps: { current: 0, layout: 'horizontal', theme: 'default' },
    alert: { theme: 'info', title: '提示信息', message: '这是一条提示消息', closable: false },
    divider: { dashed: false, content: '' },
    // 布局组件
    container: { title: '容器标题', padding: 16, borderRadius: 8, bgColor: '#ffffff', shadow: true, showFooter: false, showExtra: false, footerText: '页脚' },
    grid: { columns: 2, gutter: 16 },
    tabs: { tabs: '标签1,标签2,标签3', size: 'medium', theme: 'normal', placement: 'top' },
    collapse: { accordion: true, borderless: false, expandAll: false },
    space: { direction: 'horizontal', size: 16 },
    breadcrumb: { maxItems: 0 },
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

async function handleSave() {
  if (!pageName.value.trim()) {
    MessagePlugin.warning('请输入页面名称');
    return;
  }
  if (isSaving.value) return;
  isSaving.value = true;
  
  try {
    const payload = {
      name: pageName.value,
      pageCode: pageName.value.replace(/[^\w\u4e00-\u9fa5]/g, '_').toLowerCase() || 'unnamed_page',
      pageType: 'custom',
      layoutJson: JSON.stringify(pageElements.value),
      version: 1,
      status: pageStatus.value,
    };
    
    if (editingPageId.value) {
      await pageSchemaApi.update(editingPageId.value, { ...payload, id: editingPageId.value } as any);
    } else {
      const res = await pageSchemaApi.create(payload as any);
      if (res.data?.id || (res.data as any)?.data?.id) {
        editingPageId.value = (res.data as any)?.data?.id || res.data?.id;
      }
    }
    MessagePlugin.success('页面保存成功！');
  } catch (e: any) {
    MessagePlugin.error(e?.message || '保存失败');
  } finally {
    isSaving.value = false;
  }
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

    .designer-back-btn {
      color: rgba(255, 255, 255, 0.85) !important;
      &:hover {
        color: #fff !important;
        background: rgba(255, 255, 255, 0.12) !important;
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
          box-shadow: 0 0 0 2px rgba(232, 163, 23, 0.5);
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
          background: rgba(232, 163, 23, 0.85);
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
      background: linear-gradient(135deg, #f5a623 0%, #e8a317 100%);
      border: none;
      padding: 0 24px;
      font-weight: 600;
      color: #fff;

      &:hover {
        background: linear-gradient(135deg, #d4920a 0%, #c7800a 100%);
        box-shadow: 0 4px 12px rgba(232, 163, 23, 0.4);
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
          background: #fffdf5;
          border-color: #e8a317;
          transform: translateX(4px);
          box-shadow: 0 2px 8px rgba(232, 163, 23, 0.15);
        }

        &:active {
          cursor: grabbing;
          transform: scale(0.98);
        }

        &.dragging {
          opacity: 0.5;
          background: #fef3c7;
          border-color: #e8a317;
        }

        .component-icon-wrapper {
          width: 36px;
          height: 36px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, #f5a623 0%, #e8a317 100%);
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
        background: linear-gradient(135deg, #f5a623 0%, #e8a317 100%);
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
        background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
        border-radius: 24px;
        color: #e8a317;
        box-shadow: 0 8px 32px rgba(232, 163, 23, 0.15);
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