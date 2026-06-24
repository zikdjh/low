<template>
  <div class="app-menu-editor">
    <!-- ===== 顶部工具栏 ===== -->
    <div class="ame-header">
      <div class="ame-header-left">
        <t-button variant="outline" @click="goBack">
          <template #icon><ChevronLeftIcon size="16" /></template>
          返回
        </t-button>
        <div class="ame-title-block">
          <h2 class="ame-title">编辑菜单 · {{ app?.name || appCode }}</h2>
          <span class="ame-subtitle">草稿态，保存后需发布才会对终端用户生效</span>
        </div>
      </div>
      <div class="ame-header-right">
        <t-button theme="default" variant="outline" @click="reloadDraft">
          <template #icon><RefreshIcon size="16" /></template>
          重新加载
        </t-button>
        <t-button theme="primary" :loading="saving" @click="saveAll">
          <template #icon><SaveIcon size="16" /></template>
          保存草稿
        </t-button>
      </div>
    </div>

    <!-- ===== 主体两栏 ===== -->
    <div class="ame-body">
      <!-- 左：菜单树 -->
      <div class="ame-tree-panel">
        <div class="ame-panel-header">
          <span class="ame-panel-title">菜单结构</span>
          <div class="ame-panel-actions">
            <t-button size="small" variant="outline" @click="addNode(null, 'group')">
              <template #icon><FolderIcon size="14" /></template>
              新增分组
            </t-button>
            <t-button size="small" variant="outline" @click="addNode(null, 'menu')">
              <template #icon><AddIcon size="14" /></template>
              新增菜单
            </t-button>
          </div>
        </div>

        <div class="ame-tree-wrap">
          <t-loading v-if="loading" size="medium" />
          <t-tree
            v-else
            :data="tree"
            :keys="treeKeys"
            :draggable="true"
            :expand-all="true"
            :hover="true"
            :transition="true"
            :active-multiple="false"
            :actived="selectedKey ? [selectedKey] : []"
            line
            @active="onActive"
            @drag-end="onDragEnd"
          >
            <template #operations="{ node }">
              <div class="ame-node-ops" @click.stop>
                <t-button
                  size="small"
                  variant="text"
                  theme="primary"
                  shape="square"
                  title="在内部新增子菜单"
                  @click.stop="addNode(node, 'menu')"
                >
                  <template #icon><AddIcon size="14" /></template>
                </t-button>
                <t-button
                  size="small"
                  variant="text"
                  theme="danger"
                  shape="square"
                  title="删除此节点（含子节点）"
                  @click.stop="removeNode(node)"
                >
                  <template #icon><DeleteIcon size="14" /></template>
                </t-button>
              </div>
            </template>

            <template #label="{ node }">
              <span class="ame-node-label">
                <FolderIcon v-if="getRaw(node)?.menuType === 'group'" size="14" class="ame-node-icon" />
                <FileIcon v-else size="14" class="ame-node-icon" />
                <span class="ame-node-name">{{ node.label || '(未命名)' }}</span>
                <t-tag
                  v-if="getRaw(node)?.menuType !== 'group' && !getRaw(node)?.pageCode"
                  theme="warning"
                  variant="light"
                  size="small"
                >未绑定页面</t-tag>
              </span>
            </template>
          </t-tree>

          <div v-if="!loading && tree.length === 0" class="ame-tree-empty">
            <FolderIcon size="40" />
            <p>菜单为空，点击右上角新增</p>
          </div>
        </div>
      </div>

      <!-- 右：节点属性 -->
      <div class="ame-form-panel">
        <div class="ame-panel-header">
          <span class="ame-panel-title">节点属性</span>
        </div>

        <div v-if="!selected" class="ame-form-empty">
          <SettingIcon size="36" />
          <p>左侧选中一个节点以编辑属性</p>
        </div>

        <t-form v-else label-align="top" class="ame-form">
          <t-form-item label="节点类型">
            <t-radio-group v-model="selected.menuType" @change="touch">
              <t-radio value="group">分组（可包含子菜单）</t-radio>
              <t-radio value="menu">菜单（指向页面）</t-radio>
            </t-radio-group>
          </t-form-item>

          <t-form-item label="名称">
            <t-input v-model="selected.name" placeholder="显示名称" @change="touch" />
          </t-form-item>

          <t-form-item label="图标">
            <t-input v-model="selected.icon" placeholder="可填 emoji 或图标名（运行时按文本渲染）" @change="touch" />
          </t-form-item>

          <t-form-item v-if="selected.menuType !== 'group'" label="绑定页面（pageCode）">
            <t-select
              v-model="selected.pageCode"
              filterable
              clearable
              placeholder="从该应用下的页面中选择"
              :options="pageOptions"
              @change="onPageChange"
            />
          </t-form-item>

          <t-form-item v-if="selected.menuType !== 'group'" label="自定义路由（可选）">
            <t-input
              v-model="selected.routePath"
              :placeholder="suggestedRoutePath"
              @change="touch"
            />
          </t-form-item>

          <t-form-item label="可见">
            <t-switch v-model="selected.visible" @change="touch" />
          </t-form-item>
        </t-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import {
  ChevronLeftIcon, RefreshIcon, SaveIcon,
  FolderIcon, FileIcon, AddIcon, DeleteIcon, SettingIcon,
} from 'tdesign-icons-vue-next';
import { appMenuApi, type MenuNode, type ReorderItem } from '../../../api/lowcode/appMenu';
import { businessAppApi, type BusinessApp } from '../../../api/lowcode/businessApp';

/**
 * 应用菜单编辑器
 *
 * 操作模型：
 * - 加载后整棵草稿菜单驻留前端 reactive 状态，所有改动只动前端 tree。
 * - 保存按钮触发两步：
 *   1) PUT /menu/draft  全量重写（草稿态先 delete + insert，回填新 id）
 *   2) 拖拽改 parent / 排序的局部变更，前端在保存时直接走 PUT 一次性提交，
 *      不再单独调 /reorder（因为整树都重写了）。如果只想改顺序不重建 id，
 *      使用方应调用 reorder API；这里取简单一致策略。
 *
 * 草稿态 vs 快照态：
 *   编辑器始终操作 release_id IS NULL 的草稿菜单。运行时菜单（active release）
 *   只在发布时由 MountPhase 克隆草稿生成，不会被编辑器直接改。
 */

const route = useRoute();
const router = useRouter();

const appCode = (route.params.appCode as string) || '';

interface UiNode extends MenuNode {
  /** 前端临时 id，未保存的节点用 'tmp_xxx'，保存后会换成数据库 id */
  __uiKey: string;
  children?: UiNode[];
}

const treeKeys = { value: '__uiKey', label: 'name', children: 'children' };

const app = ref<BusinessApp | null>(null);
const tree = ref<UiNode[]>([]);
const loading = ref(true);
const saving = ref(false);
const dirty = ref(false);
const selectedKey = ref<string | null>(null);

/** 该应用所有页面（用于绑定 pageCode 下拉） */
const pageOptions = ref<{ label: string; value: string }[]>([]);
const pageRouteHints = reactive<Record<string, string>>({});

const selected = computed<UiNode | null>(() => {
  if (!selectedKey.value) return null;
  return findNode(tree.value, selectedKey.value);
});

const suggestedRoutePath = computed(() => {
  const code = selected.value?.pageCode;
  if (!code) return '/run/' + appCode + '/<pageCode>';
  return '/run/' + appCode + '/' + code;
});

// ===== 加载 =====
async function loadAll() {
  loading.value = true;
  try {
    // app 元信息（顶部标题展示用）
    try {
      const appRes = await businessAppApi.getByCode(appCode);
      app.value = (appRes.data as any)?.data || appRes.data;
    } catch {
      app.value = null;
    }

    // 该应用的页面（绑定下拉）
    try {
      const pageRes = await businessAppApi.getPages(appCode);
      const pages = ((pageRes.data as any)?.data || pageRes.data || []) as any[];
      pageOptions.value = (Array.isArray(pages) ? pages : []).map(p => ({
        label: (p.name || p.pageCode || p.code) + '（' + (p.pageCode || p.code) + '）',
        value: p.pageCode || p.code,
      }));
      for (const p of pages) {
        const code = p.pageCode || p.code;
        if (code) pageRouteHints[code] = '/run/' + appCode + '/' + code;
      }
    } catch {
      pageOptions.value = [];
    }

    // 草稿菜单
    await reloadDraft(true);
  } finally {
    loading.value = false;
  }
}

async function reloadDraft(silent = false) {
  if (!silent) loading.value = true;
  try {
    const res = await appMenuApi.getDraft(appCode);
    const raw = ((res.data as any)?.data || []) as MenuNode[];
    tree.value = toUiTree(raw);
    dirty.value = false;
    if (!silent) MessagePlugin.success('草稿已加载');
  } catch {
    tree.value = [];
    if (!silent) MessagePlugin.error('加载草稿失败');
  } finally {
    if (!silent) loading.value = false;
  }
}

// ===== 工具：原树 ↔ UI 树 =====
function toUiTree(nodes: MenuNode[]): UiNode[] {
  return nodes.map(n => ({
    ...n,
    visible: n.visible !== false,
    menuType: n.menuType || (n.children && n.children.length > 0 ? 'group' : 'menu'),
    __uiKey: n.id != null ? 'db_' + n.id : tmpKey(),
    children: n.children ? toUiTree(n.children) : [],
  }));
}

function toApiTree(nodes: UiNode[]): MenuNode[] {
  return nodes.map((n, i) => {
    const out: MenuNode = {
      id: typeof n.__uiKey === 'string' && n.__uiKey.startsWith('db_')
        ? Number(n.__uiKey.slice(3))
        : undefined,
      name: n.name || '(未命名)',
      icon: n.icon || undefined,
      sortOrder: i,
      menuType: n.menuType || 'menu',
      pageCode: n.menuType === 'group' ? undefined : (n.pageCode || undefined),
      routePath: n.menuType === 'group' ? undefined : (n.routePath || undefined),
      visible: n.visible !== false,
      children: n.children && n.children.length > 0 ? toApiTree(n.children) : undefined,
    };
    return out;
  });
}

let tmpSeq = 0;
function tmpKey() {
  tmpSeq += 1;
  return 'tmp_' + tmpSeq;
}

function findNode(nodes: UiNode[], key: string): UiNode | null {
  for (const n of nodes) {
    if (n.__uiKey === key) return n;
    if (n.children && n.children.length > 0) {
      const c = findNode(n.children, key);
      if (c) return c;
    }
  }
  return null;
}

function findParent(nodes: UiNode[], key: string, parent: UiNode | null = null): UiNode | null {
  for (const n of nodes) {
    if (n.__uiKey === key) return parent;
    if (n.children && n.children.length > 0) {
      const p = findParent(n.children, key, n);
      if (p !== null || n.children.some(c => c.__uiKey === key)) return p;
    }
  }
  return null;
}
// 当前编辑器只在删除时按 key 整树定位，未直接用到 findParent；保留导出避免再写一遍
void findParent;

/** 从 t-tree 节点对象拿到原始 UI 节点 */
function getRaw(node: any): UiNode | null {
  if (!node) return null;
  const key = node.value ?? node.__uiKey ?? node.data?.__uiKey;
  if (!key) return null;
  return findNode(tree.value, String(key));
}

// ===== 操作：增删改 =====
function touch() {
  dirty.value = true;
}

function addNode(parentNode: any | null, menuType: 'group' | 'menu') {
  const fresh: UiNode = {
    __uiKey: tmpKey(),
    name: menuType === 'group' ? '新分组' : '新菜单',
    menuType,
    visible: true,
    sortOrder: 0,
    children: [],
  };
  if (!parentNode) {
    tree.value.push(fresh);
  } else {
    const raw = getRaw(parentNode);
    if (raw) {
      raw.menuType = 'group'; // 加子节点的父自动变分组
      raw.pageCode = undefined;
      raw.routePath = undefined;
      raw.children = raw.children || [];
      raw.children.push(fresh);
    }
  }
  selectedKey.value = fresh.__uiKey;
  dirty.value = true;
}

function removeNode(node: any) {
  const raw = getRaw(node);
  if (!raw) return;
  if (raw.children && raw.children.length > 0) {
    if (!window.confirm('该节点下还有子菜单，确认一并删除？')) return;
  }
  removeFromTree(tree.value, raw.__uiKey);
  if (selectedKey.value === raw.__uiKey) selectedKey.value = null;
  dirty.value = true;
}

function removeFromTree(list: UiNode[], key: string): boolean {
  const idx = list.findIndex(x => x.__uiKey === key);
  if (idx >= 0) {
    list.splice(idx, 1);
    return true;
  }
  for (const x of list) {
    if (x.children && removeFromTree(x.children, key)) return true;
  }
  return false;
}

function onActive(values: (string | number)[]) {
  const key = values && values.length > 0 ? String(values[0]) : null;
  selectedKey.value = key;
}

function onDragEnd() {
  // t-tree 的拖拽已经直接动了 data 数组的层级，前端 tree 自然刷新；标脏即可
  dirty.value = true;
}

function onPageChange(value: any) {
  const sel = selected.value;
  if (!sel) return;
  sel.pageCode = value;
  // 自动填充 routePath 建议
  if (!sel.routePath && value) {
    sel.routePath = pageRouteHints[value] || ('/run/' + appCode + '/' + value);
  }
  dirty.value = true;
}

// ===== 保存 =====
async function saveAll() {
  // 简单校验
  if (!validate(tree.value)) return;
  saving.value = true;
  try {
    const payload = toApiTree(tree.value);
    const res = await appMenuApi.saveDraft(appCode, payload);
    const fresh = ((res.data as any)?.data || []) as MenuNode[];
    tree.value = toUiTree(fresh);
    dirty.value = false;
    // 保留当前选中（按名字 + parent 复位代价大，简单起见保存后清空选中）
    selectedKey.value = null;
    MessagePlugin.success('草稿已保存');
  } catch (e: any) {
    MessagePlugin.error('保存失败：' + (e?.response?.data?.msg || e?.message || '未知错误'));
  } finally {
    saving.value = false;
  }
}

function validate(nodes: UiNode[]): boolean {
  for (const n of nodes) {
    if (!n.name || !n.name.trim()) {
      MessagePlugin.warning('存在未命名节点，请补全名称');
      selectedKey.value = n.__uiKey;
      return false;
    }
    if (n.children && n.children.length > 0) {
      if (!validate(n.children)) return false;
    }
  }
  return true;
}

function goBack() {
  if (dirty.value && !window.confirm('有未保存的改动，确认离开？')) return;
  router.push('/lowcode/app');
}

// 阻止 unused 警告：reorder API 暂未在本编辑器使用，保存即整树重写
// 留作未来局部调整的扩展点
void appMenuApi.reorder;
void (null as unknown as ReorderItem);

onMounted(() => {
  loadAll();
});
</script>

<style scoped lang="less">
.app-menu-editor {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 64px);
  background: #f5f7fa;
}

.ame-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}

.ame-header-left,
.ame-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ame-title-block {
  display: flex;
  flex-direction: column;
}

.ame-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}

.ame-subtitle {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.ame-body {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px;
  overflow: hidden;
}

.ame-tree-panel,
.ame-form-panel {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ame-tree-panel {
  flex: 1.2;
  min-width: 320px;
}

.ame-form-panel {
  flex: 1;
  min-width: 340px;
}

.ame-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.ame-panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.ame-panel-actions {
  display: flex;
  gap: 8px;
}

.ame-tree-wrap {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}

.ame-tree-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #ccc;
  gap: 8px;
  p { margin: 0; font-size: 13px; color: #aaa; }
}

.ame-node-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.ame-node-icon {
  color: #8c8c8c;
  flex-shrink: 0;
}

.ame-node-name {
  font-size: 13px;
}

.ame-node-ops {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  margin-left: 8px;
}

.ame-form {
  padding: 16px 20px;
  overflow-y: auto;
}

.ame-form-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #ccc;
  padding: 60px 20px;
  p { color: #aaa; font-size: 13px; margin: 0; }
}
</style>
