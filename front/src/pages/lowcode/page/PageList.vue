<template>
  <div class="page-list">
    <div class="page-header">
      <div class="header-title">
        <h2>页面管理</h2>
        <p>管理和维护低代码页面</p>
      </div>
      <div class="header-actions">
        <button class="create-btn" @click="handleCreate">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 4v16m8-8H4" />
          </svg>
          新建页面
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
          :class="{ 'is-published': page.status === 'published' }"
          @click="handleView(page)"
        >
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
            <p class="card-code">{{ page.code }}</p>
          </div>
          <div class="card-footer">
            <span class="status-tag" :class="page.status">
              {{ page.status === 'published' ? '已发布' : '草稿' }}
            </span>
            <span class="version">v{{ page.version }}</span>
          </div>
          <div class="card-actions">
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
              v-model="formData.code"
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
            <select class="form-select" v-model="formData.entityId">
              <option :value="undefined">不关联实体</option>
              <option v-for="entity in entities" :key="entity.id" :value="entity.id">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { pageSchemaApi } from '../../../api/lowcode/pageSchema';
import entityMetaApi from '../../../api/lowcode/entityMeta';
import type { PageSchema, EntityMeta, PageType } from '../../../types/lowcode';
import { MessagePlugin } from 'tdesign-vue-next';

const pages = ref<PageSchema[]>([]);
const searchKeyword = ref('');
const showCreateModal = ref(false);
const editingPage = ref<PageSchema | null>(null);
const entities = ref<EntityMeta[]>([]);

const formData = ref({
  name: '',
  code: '',
  pageType: 'custom' as PageType,
  entityId: undefined as number | undefined,
});

onMounted(() => {
  loadPages();
  loadEntities();
});

async function loadPages() {
  const res = await pageSchemaApi.getAllPages();
  if (res.data.code === 0) {
    let data = res.data.data;
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase();
      data = data.filter(
        (page: PageSchema) =>
          page.name.toLowerCase().includes(keyword) ||
          page.code.toLowerCase().includes(keyword)
      );
    }
    pages.value = data;
  }
}

async function loadEntities() {
  const res = await entityMetaApi.list({ page: 1, pageSize: 100 });
  if (res.data.code === 0) {
    entities.value = res.data.data.records || [];
  }
}

function handleCreate() {
  editingPage.value = null;
  formData.value = {
    name: '',
    code: '',
    pageType: 'custom' as PageType,
    entityId: undefined,
  };
  showCreateModal.value = true;
}

function handleEdit(page: PageSchema) {
  editingPage.value = page;
  formData.value = {
    name: page.name || '',
    code: page.code || '',
    pageType: page.pageType || 'custom',
    entityId: page.entityId,
  };
  showCreateModal.value = true;
}

async function handleSubmit() {
  if (!formData.value.name || !formData.value.code) {
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
  window.open(`/lowcode/view/${page.code}`, '_blank');
}

async function handlePublish(page: PageSchema) {
  try {
    await pageSchemaApi.publish(page.id!);
    MessagePlugin.success('发布成功');
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
</script>

<style scoped>
.page-list {
  min-height: 100vh;
  background: #f5f5f5;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 3