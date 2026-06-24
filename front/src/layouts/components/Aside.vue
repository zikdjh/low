<template>
  <aside class="layout-aside" :class="{ collapsed: settingStore.isSidebarCollapsed }">
    <!-- Logo区域 -->
    <div class="sidebar-header" :class="{ collapsed: settingStore.isSidebarCollapsed }">
      <div class="logo-wrapper">
        <div class="logo-icon">
          <svg viewBox="0 0 36 36" fill="none" class="logo-svg">
            <rect width="36" height="36" rx="9" fill="url(#logoGrad)" />
            <path d="M10 12l8-4 8 4-8 4-8-4z" fill="#fff" opacity="0.85" />
            <path d="M10 18l8 4 8-4M10 24l8 4 8-4" stroke="#fff" stroke-width="1.8" stroke-linecap="round" opacity="0.7" />
            <defs>
              <linearGradient id="logoGrad" x1="0" y1="0" x2="36" y2="36">
                <stop offset="0%" stop-color="#F5A623" />
                <stop offset="100%" stop-color="#E8A317" />
              </linearGradient>
            </defs>
          </svg>
        </div>
        <span v-show="!settingStore.isSidebarCollapsed" class="logo-title">
          <span class="title-text">LowCode</span>
          <span class="title-sub">低代码开发平台</span>
        </span>
      </div>
    </div>

    <div class="sidebar-divider"></div>

    <!-- ===== 页面管理区域 ===== -->
    <div class="sidebar-section" v-show="!settingStore.isSidebarCollapsed">
      <div class="section-header" @click="toggleSection('pages')">
        <span class="section-label">
          <LayoutIcon size="14" />
          页面
        </span>
        <div class="section-header-right">
          <span class="section-count">{{ pageCount }}</span>
          <t-button
            variant="text"
            size="small"
            class="section-add-btn"
            @click.stop="goToPageList"
            title="新建页面"
          >
            <AddIcon size="14" />
          </t-button>
          <ChevronDownIcon
            size="14"
            class="section-arrow"
            :class="{ rotated: !expandedSections.includes('pages') }"
          />
        </div>
      </div>

      <div class="section-body" v-show="expandedSections.includes('pages')">
        <!-- 页面列表加载状态 -->
        <div v-if="pagesLoading" class="section-loading">
          <t-loading size="small" />
          <span>加载中...</span>
        </div>

        <!-- 页面树列表 -->
        <template v-else>
          <div
            v-for="page in pages"
            :key="page.id"
            class="tree-item"
            :class="{ active: isPageActive(page.pageCode || (page as any).code) }"
            @click="goToPageDesign(page)"
          >
            <div class="tree-item-icon">
              <FileIcon size="14" />
            </div>
            <div class="tree-item-content">
              <span class="tree-item-name">{{ page.name }}</span>
              <span class="tree-item-meta">{{ page.pageCode || (page as any).code }}</span>
            </div>
            <span class="tree-item-badge" :class="page.status">{{ page.status || 'draft' }}</span>
          </div>

          <!-- 空状态 -->
          <div v-if="pages.length === 0" class="tree-empty">
            <p>暂无页面</p>
            <t-button variant="text" size="small" theme="primary" @click="goToPageList">
              <template #icon><AddIcon /></template>
              创建第一个页面
            </t-button>
          </div>
        </template>
      </div>
    </div>

    <!-- ===== 实体模型区域 ===== -->
    <div class="sidebar-section" v-show="!settingStore.isSidebarCollapsed">
      <div class="section-header" @click="toggleSection('entities')">
        <span class="section-label">
          <DataBaseIcon size="14" />
          实体模型
        </span>
        <div class="section-header-right">
          <span class="section-count">{{ entityCount }}</span>
          <ChevronDownIcon
            size="14"
            class="section-arrow"
            :class="{ rotated: !expandedSections.includes('entities') }"
          />
        </div>
      </div>

      <div class="section-body" v-show="expandedSections.includes('entities')">
        <div v-if="entitiesLoading" class="section-loading">
          <t-loading size="small" />
          <span>加载中...</span>
        </div>

        <template v-else>
          <div
            v-for="entity in entities"
            :key="entity.id"
            class="tree-item"
            :class="{ active: isEntityActive(entity.code) }"
            @click="goToEntity(entity)"
          >
            <div class="tree-item-icon entity-icon">
              <DataBaseIcon size="14" />
            </div>
            <div class="tree-item-content">
              <span class="tree-item-name">{{ entity.name }}</span>
              <span class="tree-item-meta">{{ entity.code }} · {{ (entity as any).fields?.length || 0 }} 字段</span>
            </div>
            <span class="tree-item-badge" :class="entity.status">{{ entity.status || 'draft' }}</span>
          </div>

          <div v-if="entities.length === 0" class="tree-empty">
            <p>暂无实体模型</p>
            <t-button variant="text" size="small" theme="primary" @click="goToEntityList">
              <template #icon><AddIcon /></template>
              创建实体模型
            </t-button>
          </div>

          <!-- 查看更多 -->
          <div v-if="entityCount > maxEntityPreview" class="tree-more" @click="goToEntityList">
            查看全部 {{ entityCount }} 个实体 →
          </div>
        </template>
      </div>
    </div>

    <!-- 折叠时的简洁模式 -->
    <div class="sidebar-collapsed-nav" v-show="settingStore.isSidebarCollapsed">
      <t-tooltip content="页面管理" placement="right">
        <div class="collapsed-nav-item" :class="{ active: route.path.includes('/lowcode/page') }" @click="router.push('/lowcode/page/list')">
          <LayoutIcon size="20" />
        </div>
      </t-tooltip>
      <t-tooltip content="实体模型" placement="right">
        <div class="collapsed-nav-item" :class="{ active: route.path.includes('/lowcode/entity') }" @click="router.push('/lowcode/entity')">
          <DataBaseIcon size="20" />
        </div>
      </t-tooltip>

    </div>

    <!-- ===== 业务应用区域 ===== -->
    <div class="sidebar-section" v-show="!settingStore.isSidebarCollapsed">
      <div class="section-header" @click="toggleSection('apps')">
        <span class="section-label">
          <AppIcon size="14" />
          业务应用
        </span>
        <div class="section-header-right">
          <ChevronDownIcon
            size="14"
            class="section-arrow"
            :class="{ rotated: !expandedSections.includes('apps') }"
          />
        </div>
      </div>
      <div class="section-body" v-show="expandedSections.includes('apps')">
        <div v-if="businessApps.length === 0" class="tree-empty">
          <p>暂无业务应用</p>
        </div>
        <div v-else class="nav-items">
          <div
            v-for="app in businessApps"
            :key="app.code"
            class="tree-item nav-tree-item"
            :class="{ active: route.path.includes(`/run/${app.code}`) }"
            @click="router.push(`/run/${app.code}`)"
          >
            <div class="tree-item-icon nav-icon">
              <AppIcon size="14" />
            </div>
            <div class="tree-item-content">
              <span class="tree-item-name">{{ app.name }}</span>
              <span class="tree-item-meta" v-if="(app as any).pageCount != null">{{ (app as any).pageCount }} 个页面</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部 -->
    <div class="sidebar-footer">
      <div class="version-bar" v-show="!settingStore.isSidebarCollapsed">
        <div class="version-left">
          <span class="version-dot"></span>
          <span class="version-text">v1.0.0</span>
        </div>
        <span class="version-status">运行中</span>
      </div>
      <div v-show="settingStore.isSidebarCollapsed" class="footer-collapsed">
        <span class="version-dot-small"></span>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  AddIcon, LayoutIcon, DataBaseIcon, FileIcon, ChevronDownIcon,
  AppIcon,
} from 'tdesign-icons-vue-next';
import { useSettingStore } from '../../store';
import { pageSchemaApi } from '../../api/lowcode/pageSchema';
import entityMetaApi from '../../api/lowcode/entityMeta';
import { businessAppApi, type BusinessApp } from '../../api/lowcode/businessApp';
import type { PageSchema, EntityMeta } from '../../types/lowcode';

const route = useRoute();
const router = useRouter();
const settingStore = useSettingStore();

// 分区展开/折叠状态
const expandedSections = ref<string[]>(['apps', 'pages', 'entities']);

// 页面列表
const pages = ref<PageSchema[]>([]);
const pagesLoading = ref(false);

// 业务应用列表 — 从后端动态加载
const businessApps = ref<(BusinessApp & { pageCount?: number })[]>([]);

// 实体列表
const entities = ref<EntityMeta[]>([]);
const entitiesLoading = ref(false);
const entityCount = ref(0);
const maxEntityPreview = 8;

const pageCount = computed(() => pages.value.length);



function toggleSection(name: string) {
  const idx = expandedSections.value.indexOf(name);
  if (idx > -1) {
    expandedSections.value.splice(idx, 1);
  } else {
    expandedSections.value.push(name);
  }
}

// 加载页面列表
async function loadPages() {
  pagesLoading.value = true;
  try {
    const res = await pageSchemaApi.getAllPages();
    const data = (res as any)?.data?.data || (res as any)?.data || [];
    pages.value = Array.isArray(data) ? data : [];
  } catch {
    pages.value = [];
  } finally {
    pagesLoading.value = false;
  }
}

// 加载实体列表
async function loadEntities() {
  entitiesLoading.value = true;
  try {
    const res: any = await entityMetaApi.list({ page: 1, pageSize: maxEntityPreview });
    // Spring Data Page 返回 content + totalElements；兼容多种响应格式
    const pageData = res?.data?.data || res?.data || res || {};
    const data = pageData?.content || pageData?.records || pageData || [];
    entities.value = Array.isArray(data) ? data : [];
    entityCount.value = pageData?.totalElements || pageData?.total || entities.value.length;
  } catch {
    entities.value = [];
    entityCount.value = 0;
  } finally {
    entitiesLoading.value = false;
  }
}

onMounted(() => {
  loadPages();
  loadEntities();
  loadBusinessApps();
});

async function loadBusinessApps() {
  try {
    const res: any = await businessAppApi.getAll();
    const apps: BusinessApp[] = res?.data?.data || res?.data || [];
    // 为每个应用加载页面数量
    for (const app of apps) {
      try {
        const pageRes: any = await businessAppApi.getPages(app.code);
        const pages = pageRes?.data?.data || pageRes?.data || [];
        (app as any).pageCount = Array.isArray(pages) ? pages.length : 0;
      } catch {
        (app as any).pageCount = 0;
      }
    }
    businessApps.value = apps as any[];
  } catch {
    businessApps.value = [];
  }
}

// 判断当前激活的页面
function isPageActive(code: string): boolean {
  return route.path.includes('/lowcode/page') && route.query.code === code;
}
function isEntityActive(code: string): boolean {
  return route.path.includes('/lowcode/entity') && (route.query.code === code || route.params.entityCode === code);
}

function goToPageDesign(page: PageSchema) {
  const code = (page as any).pageCode || (page as any).code;
  router.push(`/lowcode/page/design?code=${code}&id=${page.id}`);
}
function goToPageList() {
  router.push('/lowcode/page/list');
}
function goToEntity(entity: EntityMeta) {
  router.push(`/lowcode/entity/${entity.id}`);
}
function goToEntityList() {
  router.push('/lowcode/entity');
}
</script>

<style scoped lang="less">
.layout-aside {
  width: 260px;
  background: #fafbfc;
  border-right: 1px solid #eef0f2;
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-size: 13px;

  &.collapsed { width: 64px; }
}

/* ===== Logo ===== */
.sidebar-header {
  padding: 16px 16px 12px;
  &.collapsed { padding: 14px 12px; }
}
.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo-icon {
  width: 36px; height: 36px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  .logo-svg { width: 36px; height: 36px; }
}
.logo-title {
  display: flex;
  flex-direction: column;
  gap: 1px;
  overflow: hidden;
  white-space: nowrap;
}
.title-text {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: -0.3px;
}
.title-sub {
  font-size: 11px;
  color: #999;
  letter-spacing: 0.5px;
}

.sidebar-divider {
  height: 1px;
  background: #eef0f2;
  margin: 0 12px;
  flex-shrink: 0;
}

/* ===== 分区 ===== */
.sidebar-section {
  flex-shrink: 0;
  &:not(:last-child) { margin-bottom: 2px; }
}
.sidebar-nav-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  .section-body { flex: 0; }
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  cursor: pointer;
  user-select: none;
  border-radius: 6px;
  margin: 2px 8px;
  transition: background 0.15s;
  &:hover { background: rgba(0,0,0,0.03); }
}
.section-label {
  font-size: 11px;
  font-weight: 600;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.section-header-right {
  display: flex;
  align-items: center;
  gap: 2px;
}
.section-count {
  font-size: 10px;
  font-weight: 600;
  color: #aaa;
  background: #eef0f2;
  padding: 1px 6px;
  border-radius: 8px;
  min-width: 18px;
  text-align: center;
}
.section-add-btn {
  width: 22px; height: 22px;
  border-radius: 5px;
  color: #999;
  &:hover { background: #e8e8e8; color: var(--td-brand-color, #E8A317); }
}
.section-arrow {
  color: #bbb;
  transition: transform 0.25s;
  &.rotated { transform: rotate(-90deg); }
}

.section-body {
  padding: 2px 8px;
  overflow: hidden;
}
.section-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  color: #999;
  font-size: 12px;
}

/* ===== 树节点 ===== */
.tree-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  margin: 1px 0;
  border-radius: 7px;
  cursor: pointer;
  transition: all 0.15s;
  position: relative;

  &:hover { background: rgba(0,0,0,0.04); }
  &.active {
    background: rgba(232, 163, 23, 0.08);
    color: var(--td-brand-color, #E8A317);
    .tree-item-name { color: var(--td-brand-color, #E8A317); font-weight: 600; }
    .tree-item-icon { color: var(--td-brand-color, #E8A317); }
  }
}
.tree-item-icon {
  width: 26px; height: 26px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #999;
  background: transparent;
}
.entity-icon { color: #7c8db5; }
.nav-icon { color: #888; }

.tree-item-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0;
}
.tree-item-name {
  font-size: 13px;
  font-weight: 500;
  color: #444;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}
.tree-item-meta {
  font-size: 10px;
  color: #bbb;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.tree-item-badge {
  font-size: 9px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 8px;
  flex-shrink: 0;
  text-transform: capitalize;
  &.draft { background: #fff7e6; color: #d46b08; }
  &.published { background: #f6ffed; color: #389e0d; }
  &.archived { background: #f5f5f5; color: #999; }
  &.active { background: #e6f7ff; color: #1677ff; }
}

.tree-empty {
  padding: 16px 12px;
  text-align: center;
  p { font-size: 12px; color: #bbb; margin: 0 0 6px; }
}

.tree-more {
  padding: 6px 10px;
  font-size: 11px;
  color: var(--td-brand-color, #E8A317);
  cursor: pointer;
  text-align: center;
  border-radius: 6px;
  &:hover { background: rgba(232, 163, 23, 0.06); }
}

/* 导航项 */
.nav-items { padding: 2px 8px 8px; }
.nav-tree-item {
  .tree-item-name { font-size: 13px; }
}
.nav-badge { margin-left: auto; }

/* ===== 折叠导航 ===== */
.sidebar-collapsed-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 0;
}
.collapsed-nav-item {
  width: 40px; height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #888;
  cursor: pointer;
  transition: all 0.15s;
  &:hover { background: rgba(0,0,0,0.05); color: #333; }
  &.active {
    background: rgba(232, 163, 23, 0.1);
    color: var(--td-brand-color, #E8A317);
  }
}

/* ===== 底部 ===== */
.sidebar-footer {
  flex-shrink: 0;
  margin-top: auto;
  padding: 8px 12px;
  border-top: 1px solid #eef0f2;
}
.version-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.version-left {
  display: flex;
  align-items: center;
  gap: 6px;
}
.version-dot {
  width: 7px; height: 7px;
  background: #52c41a;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.15);
}
.version-text {
  font-size: 11px;
  font-weight: 600;
  color: #999;
}
.version-status {
  font-size: 10px;
  color: #52c41a;
  background: #f6ffed;
  padding: 1px 6px;
  border-radius: 6px;
}

.footer-collapsed {
  display: flex;
  justify-content: center;
}
.version-dot-small {
  width: 6px; height: 6px;
  background: #52c41a;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.15);
  display: block;
}
</style>
