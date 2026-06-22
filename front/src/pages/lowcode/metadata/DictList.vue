<template>
  <div class="dict-list-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">字典管理</h2>
        <p class="page-subtitle">管理系统字典和数据字典项</p>
      </div>
    </div>

    <div class="dict-content">
      <!-- 字典类型列表 -->
      <div class="dict-type-panel">
        <div class="panel-header">
          <span>字典类型</span>
          <t-button size="small" theme="primary" @click="openTypeDialog()">
            <template #icon><t-icon name="add" /></template>
            新增
          </t-button>
        </div>
        <t-list :split="true">
          <t-list-item
            v-for="item in dictTypes"
            :key="item.id"
            :class="{ active: selectedDictCode === item.code }"
            @click="selectDict(item)"
          >
            <t-list-item-meta :title="item.name" :description="item.code" />
            <template #action>
              <t-space :size="2">
                <t-button size="small" variant="text" @click.stop="openTypeDialog(item)">
                  <t-icon name="edit" />
                </t-button>
                <t-popconfirm content="确认删除此字典类型？" @confirm="handleDeleteType(item.id!)">
                  <t-button size="small" variant="text" theme="danger" @click.stop>
                    <t-icon name="delete" />
                  </t-button>
                </t-popconfirm>
              </t-space>
            </template>
          </t-list-item>
        </t-list>
      </div>

      <!-- 字典项管理 -->
      <div class="dict-item-panel">
        <div class="panel-header" v-if="selectedDictCode">
          <span>字典项 — {{ selectedDictName }}</span>
          <t-space>
            <t-button size="small" variant="outline" @click="openBatchEdit()">
              <template #icon><t-icon name="edit-1" /></template>
              批量编辑
            </t-button>
            <t-button size="small" theme="primary" @click="openItemDialog()">
              <template #icon><t-icon name="add" /></template>
              新增项
            </t-button>
          </t-space>
        </div>
        <!-- Map 预览区 -->
        <div v-if="selectedDictCode && Object.keys(dictMap).length > 0" class="dict-map-preview">
          <div class="map-header">
            <span class="map-title">键值映射预览</span>
            <t-button size="small" variant="text" :loading="mapLoading" @click="loadDictMap">
              <t-icon name="refresh" />
            </t-button>
          </div>
          <div class="map-tags">
            <t-tag
              v-for="(label, value) in dictMap"
              :key="value"
              variant="light"
              size="small"
              class="map-tag"
            >
              <span class="tag-value">{{ value }}</span>
              <span class="tag-arrow">→</span>
              <span class="tag-label">{{ label }}</span>
            </t-tag>
          </div>
        </div>
        <div v-if="!selectedDictCode" class="empty-hint">
          <t-icon name="browse" size="48" />
          <p>请选择一个字典类型查看其数据项</p>
        </div>
        <t-table
          v-else
          :data="dictItems"
          :columns="itemColumns"
          :loading="itemsLoading"
          row-key="id"
          stripe
          size="small"
        >
          <template #operation="{ row }">
            <t-space :size="2">
              <t-button size="small" variant="text" @click="openItemDialog(row)">
                <t-icon name="edit" />
              </t-button>
              <t-popconfirm content="确认删除？" @confirm="handleDeleteItem(row.id!)">
                <t-button size="small" variant="text" theme="danger">
                  <t-icon name="delete" />
                </t-button>
              </t-popconfirm>
            </t-space>
          </template>
        </t-table>
      </div>
    </div>

    <!-- 字典类型对话框 -->
    <t-dialog
      v-model:visible="typeDialogVisible"
      :header="typeDialogTitle"
      width="420px"
      :confirm-btn="{ content: '确定', loading: saving }"
      @confirm="handleSaveType"
    >
      <t-form ref="typeFormRef" :data="typeForm" :rules="typeRules" label-width="80px">
        <t-form-item label="编码" name="code">
          <t-input v-model="typeForm.code" placeholder="如：order_status" />
        </t-form-item>
        <t-form-item label="名称" name="name">
          <t-input v-model="typeForm.name" placeholder="如：订单状态" />
        </t-form-item>
        <t-form-item label="描述" name="description">
          <t-textarea v-model="typeForm.description" placeholder="可选" :maxlength="256" />
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- 字典项对话框 -->
    <t-dialog
      v-model:visible="itemDialogVisible"
      :header="itemDialogTitle"
      width="420px"
      :confirm-btn="{ content: '确定', loading: saving }"
      @confirm="handleSaveItem"
    >
      <t-form ref="itemFormRef" :data="itemForm" :rules="itemRules" label-width="80px">
        <t-form-item label="值" name="value">
          <t-input v-model="itemForm.value" placeholder="如：pending" />
        </t-form-item>
        <t-form-item label="标签" name="label">
          <t-input v-model="itemForm.label" placeholder="如：待处理" />
        </t-form-item>
        <t-form-item label="排序号" name="sortOrder">
          <t-input-number v-model="itemForm.sortOrder" :min="0" style="width: 160px" />
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- 批量编辑字典项对话框 -->
    <t-dialog
      v-model:visible="batchEditVisible"
      header="批量编辑字典项"
      width="600px"
      :confirm-btn="{ content: '保存全部', loading: saving }"
      @confirm="handleBatchSave"
    >
      <p class="batch-hint">
        每行一个字典项，格式：<code>值=标签</code>，如 <code>pending=待处理</code>。
        保存后将覆盖当前字典的全部条目。
      </p>
      <t-textarea
        v-model="batchEditText"
        placeholder="值1=标签1&#10;值2=标签2&#10;值3=标签3"
        :autosize="{ minRows: 6, maxRows: 20 }"
      />
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { MessagePlugin } from 'tdesign-vue-next';
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { dictApi } from '../../../api/lowcode/dict';
import type { DictType, DictItem } from '../../../types/lowcode';

const dictTypes = ref<DictType[]>([]);
const dictItems = ref<DictItem[]>([]);
const dictMap = ref<Record<string, string>>({});
const itemsLoading = ref(false);
const mapLoading = ref(false);
const saving = ref(false);
const selectedDictCode = ref('');
const selectedDictName = ref('');

// Batch edit
const batchEditVisible = ref(false);
const batchEditText = ref('');

// Type dialog
const typeDialogVisible = ref(false);
const editingTypeId = ref<number | null>(null);
const typeFormRef = ref();
const typeForm = reactive<DictType>({ code: '', name: '', description: '' });
const typeRules = {
  code: [{ required: true, message: '请输入编码' }],
  name: [{ required: true, message: '请输入名称' }],
};

const typeDialogTitle = computed(() => editingTypeId.value ? '编辑字典类型' : '新增字典类型');

// Item dialog
const itemDialogVisible = ref(false);
const editingItemId = ref<number | null>(null);
const itemFormRef = ref();
const itemForm = reactive<DictItem>({ value: '', label: '', sortOrder: 1 });
const itemRules = {
  value: [{ required: true, message: '请输入值' }],
  label: [{ required: true, message: '请输入标签' }],
};

const itemDialogTitle = computed(() => editingItemId.value ? '编辑字典项' : '新增字典项');

const itemColumns: PrimaryTableCol[] = [
  { colKey: 'value', title: '值', width: 150 },
  { colKey: 'label', title: '标签', width: 200 },
  { colKey: 'sortOrder', title: '排序', width: 80 },
  { colKey: 'operation', title: '操作', width: 120 },
];

async function loadDictTypes() {
  try {
    const res: any = await dictApi.getAllDictTypes();
    if (res.data.code === 1) {
      dictTypes.value = res.data.data || [];
    }
  } catch (e) {
    console.error('加载字典类型失败:', e);
  }
}

async function selectDict(dict: DictType) {
  selectedDictCode.value = dict.code!;
  selectedDictName.value = dict.name!;
  await Promise.all([loadDictItems(dict.code!), loadDictMap()]);
}

async function loadDictMap() {
  if (!selectedDictCode.value) return;
  mapLoading.value = true;
  try {
    const res: any = await dictApi.getDictMap(selectedDictCode.value);
    if (res.data.code === 1) {
      dictMap.value = res.data.data || {};
    }
  } catch {
    dictMap.value = {};
  } finally {
    mapLoading.value = false;
  }
}

async function loadDictItems(dictCode: string) {
  itemsLoading.value = true;
  try {
    const res: any = await dictApi.getDictItems(dictCode);
    if (res.data.code === 1) {
      dictItems.value = res.data.data || [];
    }
  } catch (e) {
    console.error('加载字典项失败:', e);
  } finally {
    itemsLoading.value = false;
  }
}

// Type CRUD
function openTypeDialog(dict?: DictType) {
  if (dict) {
    editingTypeId.value = dict.id || null;
    Object.assign(typeForm, { code: dict.code, name: dict.name, description: dict.description || '' });
  } else {
    editingTypeId.value = null;
    Object.assign(typeForm, { code: '', name: '', description: '' });
  }
  typeFormRef.value?.clearValidate();
  typeDialogVisible.value = true;
}

async function handleSaveType() {
  const valid = await typeFormRef.value?.validate();
  if (valid !== true) return;
  saving.value = true;
  try {
    let res: any;
    if (editingTypeId.value) {
      res = await dictApi.updateDictType(editingTypeId.value, { ...typeForm });
    } else {
      res = await dictApi.createDictType({ ...typeForm });
    }
    if (res.data.code === 1) {
      MessagePlugin.success(editingTypeId.value ? '更新成功' : '创建成功');
      typeDialogVisible.value = false;
      loadDictTypes();
    } else {
      MessagePlugin.error(res.data.msg || '操作失败');
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '操作失败');
  } finally {
    saving.value = false;
  }
}

async function handleDeleteType(id: number) {
  try {
    const res: any = await dictApi.deleteDictType(id);
    if (res.data.code === 1) {
      MessagePlugin.success('已删除');
      if (dictTypes.value.find(d => d.id === id)?.code === selectedDictCode.value) {
        selectedDictCode.value = '';
      }
      loadDictTypes();
    } else {
      MessagePlugin.error(res.data.msg || '删除失败');
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '删除失败');
  }
}

// Item CRUD
function openItemDialog(item?: DictItem) {
  if (item) {
    editingItemId.value = item.id || null;
    Object.assign(itemForm, { value: item.value, label: item.label, sortOrder: item.sortOrder || 1 });
  } else {
    editingItemId.value = null;
    Object.assign(itemForm, { value: '', label: '', sortOrder: 1 });
  }
  itemFormRef.value?.clearValidate();
  itemDialogVisible.value = true;
}

async function handleSaveItem() {
  const valid = await itemFormRef.value?.validate();
  if (valid !== true) return;
  saving.value = true;
  try {
    if (editingItemId.value) {
      const res: any = await dictApi.updateDictItem(editingItemId.value, { ...itemForm, dictCode: selectedDictCode.value });
      if (res.data.code === 1) {
        MessagePlugin.success('更新成功');
        itemDialogVisible.value = false;
        loadDictItems(selectedDictCode.value);
        loadDictMap();
      } else {
        MessagePlugin.error(res.data.msg || '操作失败');
      }
    } else {
      const res: any = await dictApi.createDictItem({ ...itemForm, dictCode: selectedDictCode.value });
      if (res.data.code === 1) {
        MessagePlugin.success('添加成功');
        itemDialogVisible.value = false;
        loadDictItems(selectedDictCode.value);
        loadDictMap();
      } else {
        MessagePlugin.error(res.data.msg || '操作失败');
      }
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '操作失败');
  } finally {
    saving.value = false;
  }
}

async function handleDeleteItem(id: number) {
  try {
    const res = await dictApi.deleteDictItem(id);
    if (res.data.code === 1) {
      MessagePlugin.success('已删除');
      loadDictItems(selectedDictCode.value);
      loadDictMap();
    } else {
      MessagePlugin.error(res.data.msg || '删除失败');
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '删除失败');
  }
}

// Batch edit
function openBatchEdit() {
  // 将当前所有条目序列化为 value=label 格式
  const lines = dictItems.value.map(item => `${item.value}=${item.label}`);
  batchEditText.value = lines.join('\n');
  batchEditVisible.value = true;
}

async function handleBatchSave() {
  const text = batchEditText.value.trim();
  if (!text) {
    MessagePlugin.warning('请输入至少一个字典项');
    return;
  }
  const lines = text.split('\n').filter(l => l.trim());
  const items: DictItem[] = [];
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim();
    const eqIdx = line.indexOf('=');
    if (eqIdx <= 0) {
      MessagePlugin.warning(`第 ${i + 1} 行格式错误，应为"值=标签"：${line}`);
      return;
    }
    items.push({
      value: line.substring(0, eqIdx).trim(),
      label: line.substring(eqIdx + 1).trim(),
      sortOrder: i + 1,
    });
  }
  saving.value = true;
  try {
    const res: any = await dictApi.batchSaveItems(selectedDictCode.value, items);
    if (res.data.code === 1) {
      MessagePlugin.success(`已保存 ${items.length} 个字典项`);
      batchEditVisible.value = false;
      await loadDictItems(selectedDictCode.value);
      await loadDictMap();
    } else {
      MessagePlugin.error(res.data.msg || '操作失败');
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '操作失败');
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  loadDictTypes();
});
</script>

<style scoped lang="less">
.dict-list-page {
  padding: 0;
  background: #f8fafc;
  min-height: calc(100vh - 70px);
}

.page-header {
  padding: 24px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  .page-title {
    font-size: 24px;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 4px 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  .page-subtitle { font-size: 14px; color: #64748b; margin: 0; }
}

.dict-content {
  display: flex;
  gap: 20px;
  padding: 24px;
}

.dict-type-panel {
  width: 320px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,.04);
  border: 1px solid #f1f5f9;
  overflow: hidden;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #f1f5f9;
    font-weight: 600;
    color: #1e293b;
  }

  .t-list-item {
    cursor: pointer;
    transition: background .15s;
    &:hover { background: #f8fafc; }
    &.active {
      background: #eff6ff;
      border-left: 3px solid #667eea;
    }
  }
}

.dict-item-panel {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,.04);
  border: 1px solid #f1f5f9;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #f1f5f9;
    font-weight: 600;
    color: #1e293b;
  }

  .empty-hint {
    text-align: center;
    padding: 80px 0;
    color: #94a3b8;
    p { margin-top: 16px; }
  }
}

/* Map 预览区 */
.dict-map-preview {
  padding: 12px 16px;
  border-top: 1px solid #f1f5f9;
  background: #fafbfc;

  .map-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }
  .map-title {
    font-size: 12px;
    font-weight: 600;
    color: #94a3b8;
    text-transform: uppercase;
    letter-spacing: .5px;
  }
  .map-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }
  .map-tag {
    font-size: 12px;
    .tag-value { font-family: monospace; color: #475569; }
    .tag-arrow { margin: 0 4px; color: #94a3b8; }
    .tag-label { color: #667eea; }
  }
}

/* 批量编辑 */
.batch-hint {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 12px;
  line-height: 1.6;
  code {
    background: #f1f5f9;
    padding: 1px 6px;
    border-radius: 4px;
    font-size: 12px;
    color: #667eea;
  }
}
</style>
