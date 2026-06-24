<template>
  <div class="page-list">
    <div class="page-header">
      <div class="header-title">
        <h2>页面管理</h2>
        <p>管理和维护低代码页面</p>
      </div>
      <div class="header-actions">
        <button class="home-btn" @click="goHome">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <polyline points="9 22 9 12 15 12 15 22" />
          </svg>
          返回主页
        </button>
        <button class="create-btn" @click="handleCreate">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 4v16m8-8H4" />
          </svg>
          新建页面
        </button>
      </div>
    </div>
    
    <!-- 批量操作栏 -->
    <div v-if="selectedIds.size > 0" class="batch-bar">
      <div class="batch-info">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
          <polyline points="9 11 12 14 22 4" />
          <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
        </svg>
        <span>已选择 <b>{{ selectedIds.size }}</b> 个页面</span>
      </div>
      <div class="batch-actions">
        <button class="batch-btn aggregate" @click="handleBatchAggregate">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
            <rect x="3" y="3" width="7" height="7" />
            <rect x="14" y="3" width="7" height="7" />
            <rect x="14" y="14" width="7" height="7" />
            <rect x="3" y="14" width="7" height="7" />
          </svg>
          聚合为业务应用
        </button>
        <button class="batch-btn select-all" @click="selectAll">
          全选 ({{ pages.length }})
        </button>
        <button class="batch-btn clear" @click="clearSelection">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
            <path d="M18 6 6 18" />
            <path d="m6 6 12 12" />
          </svg>
          取消选择
        </button>
      </div>
    </div>

    <div class="page-content">
      <div class="search-bar">
        <input
          type="text"
          class="search-input"
          v-model="searchKeyword"
          placeholder="搜索页面名称或编码..."
          @keyup.enter="loadPages"
        />
        <button class="search-btn" @click="loadPages">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8" />
            <path d="m21 21-4.35-4.35" />
          </svg>
        </button>
      </div>

      <div class="page-grid">
        <div
          v-for="page in pages"
          :key="page.id"
          class="page-card"
          :class="{ 'is-published': page.status === 'published', 'is-selected': selectedIds.has(page.id!) }"
          @click="handleCardClick(page)"
        >
          <!-- 多选复选框 -->
          <div class="card-check" @click.stop="toggleSelect(page)" :class="{ checked: selectedIds.has(page.id!) }">
            <svg v-if="selectedIds.has(page.id!)" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" width="14" height="14">
              <polyline points="20 6 9 17 4 12" />
            </svg>
          </div>
          <div class="card-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7" />
              <rect x="14" y="3" width="7" height="7" />
              <rect x="14" y="14" width="7" height="7" />
              <rect x="3" y="14" width="7" height="7" />
            </svg>
          </div>
          <div class="card-info">
            <h3 class="card-name">{{ page.name }}</h3>
            <p class="card-code">{{ page.pageCode }}</p>
          </div>
          <div class="card-footer">
            <span class="status-tag" :class="page.status">
              {{ page.status === 'published' ? '已发布' : '草稿' }}
            </span>
            <span class="version">v{{ page.version }}</span>
          </div>
          <div class="card-actions">
            <button class="action-btn design" @click.stop="goDesign(page)" title="设计页面">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2" />
                <path d="M3 9h18M9 3v18" />
              </svg>
            </button>
            <button class="action-btn edit" @click.stop="handleEdit(page)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
              </svg>
            </button>
            <button
              class="action-btn publish"
              @click.stop="handlePublish(page)"
              v-if="page.status === 'draft'"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="17 8 12 3 7 8" />
                <line x1="12" y1="3" x2="12" y2="15" />
              </svg>
            </button>
            <button
              class="action-btn unpublish"
              @click.stop="handleUnpublish(page)"
              v-else
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 1 1-8 0 4 4 0 0 1 8 0z" />
              </svg>
            </button>
            <button class="action-btn delete" @click.stop="handleDelete(page)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 6h18" />
                <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6" />
                <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2" />
              </svg>
            </button>
          </div>
        </div>

        <div v-if="pages.length === 0" class="empty-state">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="7" height="7" />
            <rect x="14" y="3" width="7" height="7" />
            <rect x="14" y="14" width="7" height="7" />
            <rect x="3" y="14" width="7" height="7" />
          </svg>
          <p>暂无页面</p>
          <button class="create-btn" @click="handleCreate">创建第一个页面</button>
        </div>
      </div>
    </div>

    <div v-if="showCreateModal" class="modal-overlay" @click="showCreateModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ editingPage ? '编辑页面' : '新建页面' }}</h3>
          <button class="close-btn" @click="showCreateModal = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6 6 18" />
              <path d="m6 6 12 12" />
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-item">
            <label>页面名称</label>
            <input
              type="text"
              class="form-input"
              v-model="formData.name"
              placeholder="请输入页面名称"
            />
          </div>
          <div class="form-item">
            <label>页面编码</label>
            <input
              type="text"
              class="form-input"
              v-model="formData.pageCode"
              placeholder="请输入页面编码（小写字母和连字符）"
            />
          </div>
          <div class="form-item">
            <label>页面类型</label>
            <select class="form-select" v-model="formData.pageType">
              <option value="list">列表页</option>
              <option value="form">表单页</option>
              <option value="detail">详情页</option>
              <option value="custom">自定义页面</option>
            </select>
          </div>
          <div class="form-item">
            <label>关联实体</label>
            <select class="form-select" v-model="formData.entityCode">
              <option value="">不关联实体</option>
              <option v-for="entity in entities" :key="entity.id" :value="entity.code">
                {{ entity.name }} ({{ entity.code }})
              </option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="showCreateModal = false">取消</button>
          <button class="confirm-btn" @click="handleSubmit">保存</button>
        </div>
      </div>
    </div>

    <!-- 发布到业务应用弹窗 -->
    <div v-if="showPublishDialog" class="modal-overlay" @click="showPublishDialog = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>发布到业务应用</h3>
          <button class="close-btn" @click="showPublishDialog = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6 6 18" />
              <path d="m6 6 12 12" />
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <p class="publish-hint">将页面 <b>"{{ publishingPage?.name }}"</b> 发布到以下业务应用：</p>
          <div class="app-select-list">
            <div
              v-for="app in appList"
              :key="app.code"
              class="app-select-item"
              :class="{ selected: selectedAppCode === app.code }"
              @click="selectedAppCode = app.code"
            >
              <div class="asi-icon" :style="{ background: app.color || 'linear-gradient(135deg, #f5a623, #e8a317)' }">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                  <rect x="3" y="3" width="7" height="7" /><rect x="14" y="3" width="7" height="7" />
                  <rect x="14" y="14" width="7" height="7" /><rect x="3" y="14" width="7" height="7" />
                </svg>
              </div>
              <div class="asi-info">
                <span class="asi-name">{{ app.name }}</span>
                <span class="asi-code">{{ app.code }}</span>
              </div>
              <div class="asi-check" v-if="selectedAppCode === app.code">
                <svg viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="3" width="18" height="18">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
              </div>
            </div>

            <!-- 新建应用 -->
            <div class="app-select-item new-app" :class="{ active: showNewAppForm }" @click="showNewAppForm = !showNewAppForm">
              <div class="asi-icon" style="background: #f0f0f0; color: #999;">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                  <path d="M12 4v16m8-8H4" />
                </svg>
              </div>
              <div class="asi-info">
                <span class="asi-name">新建业务应用</span>
              </div>
            </div>

            <!-- 新建表单 -->
            <div v-if="showNewAppForm" class="new-app-form">
              <input
                type="text"
                class="form-input"
                v-model="newAppName"
                placeholder="应用名称，如：请假管理系统"
              />
              <input
                type="text"
                class="form-input"
                v-model="newAppCode"
                placeholder="应用编码，如：leave_management"
              />
              <div class="new-app-actions">
                <button class="cancel-btn" @click="showNewAppForm = false">取消</button>
                <button class="confirm-btn" @click="createAndSelectApp">创建</button>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="showPublishDialog = false">取消</button>
          <button
            class="confirm-btn"
            :disabled="!selectedAppCode && !showNewAppForm"
            @click="confirmPublishWithApp"
          >{{ showNewAppForm ? '先创建应用' : '确认发布' }}</button>
        </div>
      </div>
    </div>

    <!-- 批量聚合到业务应用弹窗 -->
    <div v-if="showBatchPublishDialog" class="modal-overlay" @click="showBatchPublishDialog = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>聚合为业务应用</h3>
          <button class="close-btn" @click="showBatchPublishDialog = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6 6 18" />
              <path d="m6 6 12 12" />
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <p class="publish-hint">
            将 <b>{{ selectedIds.size }}</b> 个页面聚合到一个业务应用中：
          </p>
          <div class="selected-pages-preview">
            <span
              v-for="pid in selectedPageList"
              :key="pid"
              class="page-tag"
            >{{ getPageNameById(pid) }}</span>
          </div>
          <div class="app-select-list" style="margin-top: 12px;">
            <div
              v-for="app in appList"
              :key="app.code"
              class="app-select-item"
              :class="{ selected: batchSelectedAppCode === app.code }"
              @click="batchSelectedAppCode = app.code"
            >
              <div class="asi-icon" :style="{ background: app.color || 'linear-gradient(135deg, #f5a623, #e8a317)' }">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                  <rect x="3" y="3" width="7" height="7" /><rect x="14" y="3" width="7" height="7" />
                  <rect x="14" y="14" width="7" height="7" /><rect x="3" y="14" width="7" height="7" />
                </svg>
              </div>
              <div class="asi-info">
                <span class="asi-name">{{ app.name }}</span>
                <span class="asi-code">{{ app.code }}</span>
              </div>
              <div class="asi-check" v-if="batchSelectedAppCode === app.code">
                <svg viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="3" width="18" height="18">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
              </div>
            </div>

            <!-- 新建应用 -->
            <div class="app-select-item new-app" :class="{ active: showBatchNewAppForm }" @click="showBatchNewAppForm = !showBatchNewAppForm">
              <div class="asi-icon" style="background: #f0f0f0; color: #999;">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                  <path d="M12 4v16m8-8H4" />
                </svg>
              </div>
              <div class="asi-info">
                <span class="asi-name">新建业务应用</span>
              </div>
            </div>

            <!-- 新建表单 -->
            <div v-if="showBatchNewAppForm" class="new-app-form">
              <input
                type="text"
                class="form-input"
                v-model="batchNewAppName"
                placeholder="应用名称，如：请假管理系统"
              />
              <input
                type="text"
                class="form-input"
                v-model="batchNewAppCode"
                placeholder="应用编码，如：leave_management"
              />
              <div class="new-app-actions">
                <button class="cancel-btn" @click="showBatchNewAppForm = false">取消</button>
                <button class="confirm-btn" @click="createAndSelectBatchApp">创建</button>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="showBatchPublishDialog = false">取消</button>
          <button
            class="confirm-btn"
            :disabled="!batchSelectedAppCode && !showBatchNewAppForm"
            @click="confirmBatchPublish"
          >{{ showBatchNewAppForm ? '先创建应用' : '确认聚合' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { pageSchemaApi } from '../../../api/lowcode/pageSchema';
import entityMetaApi from '../../../api/lowcode/entityMeta';
import { businessAppApi, type BusinessApp } from '../../../api/lowcode/businessApp';
import type { PageSchema, EntityMeta, PageType } from '../../../types/lowcode';
import { MessagePlugin } from 'tdesign-vue-next';

const router = useRouter();

const pages = ref<PageSchema[]>([]);
const searchKeyword = ref('');
const showCreateModal = ref(false);
const editingPage = ref<PageSchema | null>(null);
const entities = ref<EntityMeta[]>([]);

// 多选相关
const selectedIds = ref<Set<number>>(new Set());

// 发布到业务应用相关（单个）
const showPublishDialog = ref(false);
const publishingPage = ref<PageSchema | null>(null);
const appList = ref<BusinessApp[]>([]);
const selectedAppCode = ref('');
const showNewAppForm = ref(false);
const newAppName = ref('');
const newAppCode = ref('');

// 批量聚合到业务应用相关
const showBatchPublishDialog = ref(false);
const batchSelectedAppCode = ref('');
const showBatchNewAppForm = ref(false);
const batchNewAppName = ref('');
const batchNewAppCode = ref('');

// 已选页面ID列表（用于展示）
const selectedPageList = ref<number[]>([]);

const formData = ref({
  name: '',
  pageCode: '',
  pageType: 'custom' as PageType,
  entityCode: '' as string,
});

onMounted(() => {
  loadPages();
  loadEntities();
});

async function loadPages() {
  try {
    const res = await pageSchemaApi.getAllPages();
    if (res.data.code === 1) {
      let data = res.data.data;
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase();
        data = data.filter(
          (page: PageSchema) =>
            page.name.toLowerCase().includes(keyword) ||
            page.pageCode.toLowerCase().includes(keyword)
        );
      }
      pages.value = data;
    } else {
      // 尝试直接从data获取
      const data = res.data as any;
      pages.value = Array.isArray(data) ? data : (data?.data || (data?.records || []));
    }
  } catch (e: any) {
    MessagePlugin.error(e?.message || '加载页面列表失败');
  }
}

async function loadEntities() {
  try {
    const res = await entityMetaApi.list({ page: 1, pageSize: 100 });
    if (res.data.code === 1) {
      entities.value = res.data.data.records || res.data.data?.content || [];
    }
  } catch { /* 静默处理 */ }
}

function goHome() {
  router.push('/home');
}

function goDesign(page: PageSchema) {
  router.push({ path: '/lowcode/page/design', query: { id: String(page.id) } });
}

function handleCreate() {
  editingPage.value = null;
  formData.value = {
    name: '',
    pageCode: '',
    pageType: 'custom' as PageType,
    entityCode: '',
  };
  showCreateModal.value = true;
}

function handleEdit(page: PageSchema) {
  editingPage.value = page;
  formData.value = {
    name: page.name || '',
    pageCode: page.pageCode || '',
    pageType: page.pageType || 'custom',
    entityCode: page.entityCode || '',
  };
  showCreateModal.value = true;
}

async function handleSubmit() {
  if (!formData.value.name || !formData.value.pageCode) {
    MessagePlugin.warning('请填写页面名称和编码');
    return;
  }

  try {
    if (editingPage.value) {
      await pageSchemaApi.update(editingPage.value.id!, {
        ...editingPage.value,
        ...formData.value,
      });
      MessagePlugin.success('更新成功');
    } else {
      await pageSchemaApi.create({
        ...formData.value,
        layoutJson: JSON.stringify([]),
        version: 1,
        status: 'draft',
      });
      MessagePlugin.success('创建成功');
    }
    showCreateModal.value = false;
    loadPages();
  } catch (error) {
    MessagePlugin.error('操作失败');
  }
}

function handleView(page: PageSchema) {
  router.push({ path: '/lowcode/page/view', query: { code: page.pageCode } });
}

async function handlePublish(page: PageSchema) {
  // 打开发布到业务应用弹窗
  publishingPage.value = page;
  selectedAppCode.value = '';
  showNewAppForm.value = false;
  newAppName.value = '';
  newAppCode.value = '';
  // 加载业务应用列表
  try {
    const res = await businessAppApi.getAll();
    if (res.data.code === 1) {
      appList.value = Array.isArray(res.data.data) ? res.data.data : [];
    }
  } catch {
    appList.value = [];
  }
  showPublishDialog.value = true;
}

async function createAndSelectApp() {
  if (!newAppName.value || !newAppCode.value) {
    MessagePlugin.warning('请填写应用名称和编码');
    return;
  }
  try {
    const res = await businessAppApi.create({
      name: newAppName.value,
      code: newAppCode.value,
      description: '',
      color: '#e8a317',
    });
    const createdApp = (res as any)?.data?.data || (res as any)?.data;
    const code = createdApp?.code || newAppCode.value;
    // 刷新列表
    const listRes = await businessAppApi.getAll();
    if (listRes.data.code === 1) {
      appList.value = Array.isArray(listRes.data.data) ? listRes.data.data : [];
    }
    selectedAppCode.value = code;
    showNewAppForm.value = false;
    MessagePlugin.success('应用已创建');
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '创建应用失败');
  }
}

async function confirmPublishWithApp() {
  if (!selectedAppCode.value || !publishingPage.value) {
    MessagePlugin.warning('请选择业务应用');
    return;
  }
  try {
    // 先发布页面
    await pageSchemaApi.publish(publishingPage.value.id!);
    // 再分配到应用
    await businessAppApi.assignPage(selectedAppCode.value, publishingPage.value.id!);
    MessagePlugin.success(`已发布到 "${selectedAppCode.value}" 业务应用`);
    showPublishDialog.value = false;
    loadPages();
  } catch (error) {
    MessagePlugin.error('发布失败');
  }
}

async function handleUnpublish(page: PageSchema) {
  try {
    await pageSchemaApi.unpublish(page.id!);
    MessagePlugin.success('已取消发布');
    loadPages();
  } catch (error) {
    MessagePlugin.error('操作失败');
  }
}

async function handleDelete(page: PageSchema) {
  if (!confirm(`确定要删除页面 "${page.name}" 吗？`)) {
    return;
  }
  try {
    await pageSchemaApi.delete(page.id!);
    MessagePlugin.success('删除成功');
    loadPages();
  } catch (error) {
    MessagePlugin.error('删除失败');
  }
}

// ====== 多选与批量聚合 ======

/** 切换单页选中 */
function toggleSelect(page: PageSchema) {
  const id = page.id!;
  const newSet = new Set(selectedIds.value);
  if (newSet.has(id)) {
    newSet.delete(id);
  } else {
    newSet.add(id);
  }
  selectedIds.value = newSet;
}

/** 点击卡片：有选择时切换选中，否则查看页面 */
function handleCardClick(page: PageSchema) {
  if (selectedIds.value.size > 0) {
    toggleSelect(page);
  } else {
    handleView(page);
  }
}

/** 全选 */
function selectAll() {
  selectedIds.value = new Set(pages.value.map(p => p.id!));
}

/** 取消全选 */
function clearSelection() {
  selectedIds.value = new Set();
}

/** 按ID获取页面名称 */
function getPageNameById(id: number) {
  return pages.value.find(p => p.id === id)?.name || `#${id}`;
}

/** 打开批量聚合弹窗 */
async function handleBatchAggregate() {
  if (selectedIds.value.size === 0) {
    MessagePlugin.warning('请先选择页面');
    return;
  }
  batchSelectedAppCode.value = '';
  showBatchNewAppForm.value = false;
  batchNewAppName.value = '';
  batchNewAppCode.value = '';
  selectedPageList.value = Array.from(selectedIds.value);
  // 加载业务应用列表
  try {
    const res = await businessAppApi.getAll();
    if (res.data.code === 1) {
      appList.value = Array.isArray(res.data.data) ? res.data.data : [];
    }
  } catch {
    appList.value = [];
  }
  showBatchPublishDialog.value = true;
}

/** 批量模式：创建并选中新应用 */
async function createAndSelectBatchApp() {
  if (!batchNewAppName.value || !batchNewAppCode.value) {
    MessagePlugin.warning('请填写应用名称和编码');
    return;
  }
  try {
    const res = await businessAppApi.create({
      name: batchNewAppName.value,
      code: batchNewAppCode.value,
      description: '',
      color: '#e8a317',
    });
    const createdApp = (res as any)?.data?.data || (res as any)?.data;
    const code = createdApp?.code || batchNewAppCode.value;
    // 刷新列表
    const listRes = await businessAppApi.getAll();
    if (listRes.data.code === 1) {
      appList.value = Array.isArray(listRes.data.data) ? listRes.data.data : [];
    }
    batchSelectedAppCode.value = code;
    showBatchNewAppForm.value = false;
    MessagePlugin.success('应用已创建');
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '创建应用失败');
  }
}

/** 确认批量聚合 */
async function confirmBatchPublish() {
  if (!batchSelectedAppCode.value) {
    MessagePlugin.warning('请选择业务应用');
    return;
  }
  const pageIds = Array.from(selectedIds.value);
  if (pageIds.length === 0) {
    MessagePlugin.warning('请选择至少一个页面');
    return;
  }
  try {
    await businessAppApi.assignBatchPages(batchSelectedAppCode.value, pageIds);
    MessagePlugin.success(`已将 ${pageIds.length} 个页面聚合到 "${batchSelectedAppCode.value}" 业务应用`);
    showBatchPublishDialog.value = false;
    clearSelection();
    loadPages();
  } catch (error: any) {
    MessagePlugin.error(error?.response?.data?.msg || '聚合失败');
  }
}
</script>

<style scoped>
.page-list {
  min-height: calc(100vh - 64px);
  background: #f7f8fa;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header-title h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 6px;
}
.header-title p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.home-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #f5f5f5;
  color: #555;
  border: 1.5px solid #e5e5e5;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s;
}
.home-btn:hover {
  background: #eee;
  color: #333;
  border-color: #ccc;
  transform: translateY(-1px);
}
.home-btn svg {
  width: 18px;
  height: 18px;
}

.create-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #f5a623, #e8a317);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s;
  box-shadow: 0 2px 8px rgba(232, 163, 23, 0.3);
}
.create-btn:hover {
  box-shadow: 0 4px 16px rgba(232, 163, 23, 0.4);
  transform: translateY(-1px);
}
.create-btn svg {
  width: 18px;
  height: 18px;
}

/* 批量操作栏 */
.batch-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #eff6ff, #f0f9ff);
  border: 1.5px solid #bfdbfe;
  border-radius: 14px;
  padding: 14px 20px;
  margin-bottom: 16px;
  animation: slideDown 0.2s ease;
}
@keyframes slideDown { from { opacity: 0; transform: translateY(-8px); } to { opacity: 1; transform: translateY(0); } }
.batch-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #1e40af;
}
.batch-info svg { color: #2563eb; }
.batch-info b { color: #1d4ed8; }
.batch-actions {
  display: flex;
  gap: 8px;
}
.batch-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}
.batch-btn.aggregate {
  background: linear-gradient(135deg, #f5a623, #e8a317);
  color: #fff;
  box-shadow: 0 2px 8px rgba(232, 163, 23, 0.3);
}
.batch-btn.aggregate:hover {
  box-shadow: 0 4px 14px rgba(232, 163, 23, 0.45);
  transform: translateY(-1px);
}
.batch-btn.select-all {
  background: #fff;
  color: #555;
  border: 1.5px solid #e5e5e5;
}
.batch-btn.select-all:hover { background: #f5f5f5; }
.batch-btn.clear {
  background: #fff;
  color: #999;
  border: 1.5px solid #e5e5e5;
}
.batch-btn.clear:hover { background: #f5f5f5; color: #ef4444; border-color: #fecaca; }

/* 卡片多选复选框 */
.card-check {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 22px;
  height: 22px;
  border: 2px solid #d1d5db;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 2;
  background: #fff;
}
.card-check:hover {
  border-color: #e8a317;
  background: #fef9ef;
}
.card-check.checked {
  background: #e8a317;
  border-color: #e8a317;
  color: #fff;
}
.page-card.is-selected {
  border-color: #e8a317;
  box-shadow: 0 0 0 2px rgba(232, 163, 23, 0.15);
  background: #fffdf5;
}

/* 批量弹窗 - 已选页面预览 */
.selected-pages-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 4px;
}
.page-tag {
  display: inline-block;
  padding: 4px 10px;
  background: #fef3c7;
  color: #b45309;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.page-content {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
}
.search-input {
  flex: 1;
  padding: 10px 16px;
  border: 1.5px solid #e5e5e5;
  border-radius: 10px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
}
.search-input:focus {
  border-color: #e8a317;
  box-shadow: 0 0 0 3px rgba(232, 163, 23, 0.08);
}
.search-btn {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  color: #999;
  transition: all 0.2s;
}
.search-btn:hover {
  background: #e8a317;
  color: #fff;
}
.search-btn svg { width: 18px; height: 18px; }

.page-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.page-card {
  background: #fff;
  border: 1.5px solid #f0f0f0;
  border-radius: 14px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.25s;
  position: relative;
  overflow: hidden;
}
.page-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  border-color: #e8a317;
}
.page-card.is-published {
  border-color: #d4edda;
}

.card-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #f5a623, #e8a317);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 14px;
}
.card-icon svg { width: 20px; height: 20px; }
.card-info { margin-bottom: 14px; }
.card-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px;
}
.card-code {
  font-size: 12px;
  color: #999;
  margin: 0;
  font-family: 'JetBrains Mono', monospace;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.status-tag {
  font-size: 12px;
  font-weight: 500;
  padding: 3px 10px;
  border-radius: 20px;
}
.status-tag.draft { background: #fef3c7; color: #b45309; }
.status-tag.published { background: #dcfce7; color: #15803d; }
.version { font-size: 12px; color: #ccc; }

.card-actions {
  display: flex;
  gap: 6px;
  border-top: 1px solid #f5f5f5;
  padding-top: 14px;
}
.action-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #eee;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s;
  color: #888;
}
.action-btn svg { width: 15px; height: 15px; }
.action-btn.edit:hover { background: #e8a317; color: #fff; border-color: #e8a317; }
.action-btn.design:hover { background: #6366f1; color: #fff; border-color: #6366f1; }
.action-btn.publish:hover { background: #10b981; color: #fff; border-color: #10b981; }
.action-btn.unpublish:hover { background: #f59e0b; color: #fff; border-color: #f59e0b; }
.action-btn.delete:hover { background: #ef4444; color: #fff; border-color: #ef4444; }

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}
.empty-state svg { width: 64px; height: 64px; margin-bottom: 16px; opacity: 0.3; }
.empty-state p { font-size: 16px; margin: 0 0 20px; }

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
  animation: fadeIn 0.2s ease;
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
.modal-content {
  background: #fff;
  border-radius: 16px;
  width: 480px;
  max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
  animation: slideUp 0.3s ease;
}
@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}
.modal-header h3 { font-size: 18px; font-weight: 600; color: #1a1a1a; margin: 0; }
.close-btn {
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  border: none; border-radius: 8px; background: transparent;
  cursor: pointer; color: #999; transition: all 0.2s;
}
.close-btn:hover { background: #f5f5f5; color: #333; }
.close-btn svg { width: 18px; height: 18px; }
.modal-body { padding: 24px; }
.form-item { margin-bottom: 16px; }
.form-item label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}
.form-input, .form-select {
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid #e5e5e5;
  border-radius: 10px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
  box-sizing: border-box;
  background: #fff;
}
.form-input:focus, .form-select:focus {
  border-color: #e8a317;
  box-shadow: 0 0 0 3px rgba(232, 163, 23, 0.08);
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}
.cancel-btn {
  padding: 8px 20px;
  border: 1.5px solid #e5e5e5;
  border-radius: 10px;
  background: #fff;
  color: #666;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}
.cancel-btn:hover { background: #f5f5f5; }
.confirm-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #f5a623, #e8a317);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.confirm-btn:hover { box-shadow: 0 4px 12px rgba(232, 163, 23, 0.4); }
.confirm-btn:disabled { opacity: 0.5; cursor: not-allowed; box-shadow: none; }

/* 发布到业务应用弹窗 */
.publish-hint {
  font-size: 14px;
  color: #555;
  margin: 0 0 16px;
  b { color: #1a1a1a; }
}
.app-select-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 300px;
  overflow-y: auto;
}
.app-select-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border: 1.5px solid #e5e5e5;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover { border-color: #e8a317; background: #fef9ef; }
  &.selected { border-color: #e8a317; background: rgba(232, 163, 23, 0.06); }
  &.new-app { border-style: dashed; }
  &.new-app.active { border-color: #10b981; }
}
.asi-icon {
  width: 36px; height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.asi-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.asi-name { font-size: 14px; font-weight: 500; color: #1a1a1a; }
.asi-code { font-size: 12px; color: #999; font-family: 'JetBrains Mono', monospace; }
.asi-check { flex-shrink: 0; }
.new-app-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 8px 0;
  .form-input {
    padding: 8px 12px;
    border: 1.5px solid #e5e5e5;
    border-radius: 8px;
    font-size: 13px;
    outline: none;
    &:focus { border-color: #e8a317; }
  }
  .new-app-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}
</style>