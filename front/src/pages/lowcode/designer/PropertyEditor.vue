<template>
  <div class="property-editor">
    <div class="editor-header">
      <h3>属性面板</h3>
    </div>
    <div class="editor-content">
      <template v-if="selectedComponent">
        <div class="section">
          <div class="section-title">基本属性</div>
          <t-form-item label="组件名称" label-width="70">
            <t-input
              v-model="localProps.label"
              @input="updateLabel"
              size="small"
            />
          </t-form-item>
        </div>
        
        <div class="section">
          <div class="section-title">组件属性</div>
          <div v-if="propsSchema && propsSchema.properties" class="schema-props">
            <t-form-item
              v-for="(prop, key) in propsSchema.properties"
              :key="key"
              :label="prop.title || key"
              label-width="70"
            >
              <t-input
                v-if="prop.type === 'string'"
                :value="localProps[key]"
                @input="updateProp(key, ($event.target as HTMLInputElement).value)"
                size="small"
              />
              <t-input
                v-else-if="prop.type === 'number'"
                type="number"
                :value="localProps[key]"
                @input="updateProp(key, Number(($event.target as HTMLInputElement).value))"
                size="small"
              />
              <t-switch
                v-else-if="prop.type === 'boolean'"
                :value="localProps[key]"
                @change="updateProp(key, ($event.target as HTMLInputElement).checked)"
              />
              <t-select
                v-else-if="prop.enum"
                :value="localProps[key]"
                @change="updateProp(key, $event)"
                size="small"
              >
                <t-option v-for="opt in prop.enum" :key="opt" :value="opt">{{ opt }}</t-option>
              </t-select>
            </t-form-item>
          </div>
          <div v-else class="empty-props">
            <p>暂无可配置属性</p>
          </div>
        </div>

        <div class="section">
          <div class="section-title">样式</div>
          <t-form-item label="宽度" label-width="70">
            <t-input
              :value="localStyle.width"
              @input="updateStyle('width', ($event.target as HTMLInputElement).value)"
              placeholder="100%"
              size="small"
            />
          </t-form-item>
          <t-form-item label="高度" label-width="70">
            <t-input
              :value="localStyle.height"
              @input="updateStyle('height', ($event.target as HTMLInputElement).value)"
              placeholder="auto"
              size="small"
            />
          </t-form-item>
          <t-form-item label="外边距" label-width="70">
            <t-input
              :value="localStyle.margin"
              @input="updateStyle('margin', ($event.target as HTMLInputElement).value)"
              placeholder="0"
              size="small"
            />
          </t-form-item>
          <t-form-item label="内边距" label-width="70">
            <t-input
              :value="localStyle.padding"
              @input="updateStyle('padding', ($event.target as HTMLInputElement).value)"
              placeholder="0"
              size="small"
            />
          </t-form-item>
        </div>
      </template>
      <template v-else>
        <div class="empty-tip">
          <MousePointerIcon size="48" />
          <p>选择组件以编辑属性</p>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useDesignerStore } from '../../../store/modules/designer';
import { componentDefApi } from '../../../api/lowcode/componentDef';
import MousePointerIcon from 'tdesign-icons-vue-next';

const designerStore = useDesignerStore();
const { selectedComponent, updateComponentProps, updateComponent } = designerStore;

const localProps = ref<Record<string, any>>({});
const localStyle = ref<Record<string, string>>({});
const propsSchema = ref<any>(null);

watch(selectedComponent, (component) => {
  if (component) {
    localProps.value = { ...component.props };
    localStyle.value = { ...component.style };
    loadPropsSchema(component.compKey);
  } else {
    localProps.value = {};
    localStyle.value = {};
    propsSchema.value = null;
  }
}, { immediate: true });

async function loadPropsSchema(compKey: string) {
  const res = await componentDefApi.getByCompKey(compKey);
  if (res.code === 0 && res.data) {
    try {
      propsSchema.value = JSON.parse(res.data.propsSchemaJson || '{}');
    } catch {
      propsSchema.value = null;
    }
  }
}

function updateProp(key: string, value: any) {
  localProps.value[key] = value;
  if (selectedComponent.value) {
    updateComponentProps(selectedComponent.value.id, { [key]: value });
  }
}

function updateLabel() {
  if (selectedComponent.value) {
    updateComponent(selectedComponent.value.id, { label: localProps.value.label });
  }
}

function updateStyle(key: string, value: string) {
  localStyle.value[key] = value;
  if (selectedComponent.value) {
    const newStyle = { ...selectedComponent.value.style, [key]: value };
    updateComponent(selectedComponent.value.id, { style: newStyle });
  }
}
</script>

<style scoped lang="less">
.property-editor {
  width: 320px;
  height: 100%;
  background: #ffffff;
  border-left: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
}

.editor-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
}

.editor-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.editor-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 12px;
  padding-left: 4px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.schema-props {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.empty-props, .empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
  color: #94a3b8;
}

.empty-tip :deep(.t-icon) {
  margin-bottom: 12px;
  color: #cbd5e1;
}

.empty-props p, .empty-tip p {
  margin: 0;
  font-size: 13px;
}
</style>
