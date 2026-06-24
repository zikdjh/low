<template>
  <div class="app-runtime">
    <!-- ===== 左侧导航栏 ===== -->
    <aside class="ar-sidebar">
      <!-- 应用头部 -->
      <div class="ar-sidebar-brand">
        <div class="ar-brand-icon" :style="{ background: app?.color || 'linear-gradient(135deg, #667eea, #764ba2)' }">
          <AppIcon size="22" />
        </div>
        <div class="ar-brand-info">
          <h3>{{ app?.name || '业务应用' }}</h3>
        </div>
      </div>

      <!-- 导航菜单 -->
      <nav class="ar-nav">
        <div class="ar-nav-label">导航菜单</div>
        <div
          v-for="page in pages"
          :key="page.id"
          class="ar-nav-item"
          :class="{ active: activePage?.id === page.id }"
          @click="switchPage(page)"
        >
          <div class="ar-nav-dot" :class="{ on: activePage?.id === page.id }"></div>
          <FileIcon size="16" class="ar-nav-icon" />
          <span class="ar-nav-text">{{ page.name }}</span>
          <t-tag v-if="page.status !== 'published'" theme="warning" variant="light" size="small" class="ar-nav-badge">草稿</t-tag>
        </div>
        <div v-if="pages.length === 0 && !loading" class="ar-nav-empty">
          <FileIcon size="20" />
          <p>暂无页面</p>
        </div>
      </nav>

      <!-- 底部操作 -->
      <div class="ar-sidebar-footer">
        <t-button variant="outline" block theme="default" @click="goHome">
          <template #icon><HomeIcon size="16" /></template>
          返回主页
        </t-button>
      </div>
    </aside>

    <!-- ===== 主内容区 ===== -->
    <div class="ar-main">
      <!-- 顶部栏 -->
      <header class="ar-topbar">
        <div class="ar-topbar-left">
          <ComponentBreadcrumbIcon size="18" />
          <span class="ar-breadcrumb-app" @click="activePage = null">{{ app?.name }}</span>
          <template v-if="activePage">
            <ChevronRightIcon size="14" class="ar-breadcrumb-sep" />
            <span class="ar-breadcrumb-page">{{ activePage.name }}</span>
          </template>
        </div>
        <div class="ar-topbar-right">
          <t-tag v-if="activePage" :theme="activePage.status === 'published' ? 'success' : 'warning'" variant="light">
            {{ activePage.status === 'published' ? '已发布' : '草稿' }}
          </t-tag>
          <span class="ar-time">{{ currentTime }}</span>
        </div>
      </header>

      <!-- 页面内容区 -->
      <div class="ar-content">
        <!-- 加载状态 -->
        <div v-if="loading" class="ar-loading">
          <t-loading size="large" text="加载应用..." />
        </div>

        <!-- 首页/欢迎页（未选中页面时） -->
        <div v-else-if="!activePage" class="ar-welcome">
          <div class="ar-welcome-icon" :style="{ background: app?.color || 'linear-gradient(135deg, #667eea, #764ba2)' }">
            <AppIcon size="48" />
          </div>
          <h2>{{ app?.name || '业务应用' }}</h2>
          <p class="ar-welcome-desc">{{ app?.description || '由低代码平台构建的业务系统' }}</p>
          <div class="ar-welcome-stats">
            <div class="ar-stat-item">
              <span class="ar-stat-num">{{ pages.length }}</span>
              <span class="ar-stat-label">功能页面</span>
            </div>
            <div class="ar-stat-item">
              <span class="ar-stat-num">{{ publishedCount }}</span>
              <span class="ar-stat-label">已发布</span>
            </div>
          </div>
          <div v-if="pages.length > 0" class="ar-welcome-nav">
            <p>请从左侧菜单选择功能：</p>
            <div class="ar-shortcut-grid">
              <div
                v-for="page in pages"
                :key="page.id"
                class="ar-shortcut-card"
                @click="switchPage(page)"
              >
                <div class="ar-shortcut-icon">
                  <FileIcon size="20" />
                </div>
                <span>{{ page.name }}</span>
                <ChevronRightIcon size="14" class="ar-shortcut-arrow" />
              </div>
            </div>
          </div>
        </div>

        <!-- 页面渲染区 -->
        <div v-else class="ar-page-viewport">
          <div class="ar-page-content">
            <div
              v-for="element in pageElements"
              :key="element.id"
              class="page-element"
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
                :enable-events="true"
              />
            </div>
            <div v-if="pageElements.length === 0" class="ar-empty-page">
              <LayoutIcon size="56" />
              <h3>空白页面</h3>
              <p>该页面尚未添加任何组件，请前往页面设计器编辑</p>
              <t-button
                theme="primary"
                variant="outline"
                size="small"
                @click="goDesign(activePage)"
              >
                前往设计
              </t-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, defineAsyncComponent } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { businessAppApi, type BusinessApp } from '../../../api/lowcode/businessApp';
import { pageSchemaApi } from '../../../api/lowcode/pageSchema';
import {
  AppIcon, FileIcon, ChevronRightIcon, HomeIcon,
  ComponentBreadcrumbIcon, LayoutIcon,
} from 'tdesign-icons-vue-next';

const route = useRoute();
const router = useRouter();

const appCode = (route.params.appCode as string) || '';
const app = ref<BusinessApp | null>(null);
const pages = ref<any[]>([]);
const activePage = ref<any>(null);
const pageElements = ref<any[]>([]);
const loading = ref(true);

const publishedCount = computed(() => pages.value.filter(p => p.status === 'published').length);

// 当前时间
const currentTime = ref('');
let timeTimer: ReturnType<typeof setInterval> | null = null;

function updateTime() {
  const now = new Date();
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

// ===== 组件映射表（与 PageViewer 保持一致） =====
const componentMap: Record<string, any> = {
  text: () => import('../page/components/TextElement.vue'),
  button: () => import('../page/components/ButtonElement.vue'),
  link: () => import('../page/components/LinkElement.vue'),
  image: () => import('../page/components/ImageElement.vue'),
  input: () => import('../page/components/InputElement.vue'),
  textarea: () => import('../page/components/TextareaElement.vue'),
  inputNumber: () => import('../page/components/InputNumberElement.vue'),
  select: () => import('../page/components/SelectElement.vue'),
  date: () => import('../page/components/DateElement.vue'),
  time: () => import('../page/components/TimeElement.vue'),
  switch: () => import('../page/components/SwitchElement.vue'),
  checkbox: () => import('../page/components/CheckboxElement.vue'),
  radio: () => import('../page/components/RadioElement.vue'),
  slider: () => import('../page/components/SliderElement.vue'),
  rate: () => import('../page/components/RateElement.vue'),
  upload: () => import('../page/components/UploadElement.vue'),
  table: () => import('../page/components/TableElement.vue'),
  form: () => import('../page/components/FormElement.vue'),
  list: () => import('../page/components/ListElement.vue'),
  chart: () => import('../page/components/ChartElement.vue'),
  card: () => import('../page/components/CardElement.vue'),
  tag: () => import('../page/components/TagElement.vue'),
  progress: () => import('../page/components/ProgressElement.vue'),
  steps: () => import('../page/components/StepsElement.vue'),
  alert: () => import('../page/components/AlertElement.vue'),
  divider: () => import('../page/components/DividerElement.vue'),
  container: () => import('../page/components/ContainerElement.vue'),
  grid: () => import('../page/components/GridElement.vue'),
  tabs: () => import('../page/components/TabsElement.vue'),
  collapse: () => import('../page/components/CollapseElement.vue'),
  space: () => import('../page/components/SpaceElement.vue'),
  breadcrumb: () => import('../page/components/BreadcrumbElement.vue'),
};

function getElementComponent(type: string) {
  const loader = componentMap[type];
  if (loader) {
    return defineAsyncComponent(loader);
  }
  return null;
}

// ===== 加载数据 =====
async function loadData() {
  loading.value = true;
  try {
    // 加载应用信息
    const appRes = await businessAppApi.getByCode(appCode);
    app.value = (appRes.data as any)?.data || appRes.data;

    // 加载应用页面
    const pageRes = await businessAppApi.getPages(appCode);
    const data = (pageRes.data as any)?.data || pageRes.data || [];
    pages.value = Array.isArray(data) ? data : [];
  } catch {
    app.value = null;
    pages.value = [];
  } finally {
    loading.value = false;
  }
}

// ===== 切换页面 =====
async function switchPage(page: any) {
  activePage.value = page;
  pageElements.value = [];
  
  const code = page.pageCode || page.code;
  if (!code) return;

  try {
    const res = await pageSchemaApi.getByPageCode(code);
    if ((res.data as any)?.code === 1 && (res.data as any)?.data) {
      const pageData = (res.data as any).data;
      try {
        const layout = typeof pageData.layoutJson === 'string'
          ? JSON.parse(pageData.layoutJson)
          : (pageData.layoutJson || []);
        pageElements.value = Array.isArray(layout) ? layout : [];
      } catch { pageElements.value = []; }
    } else if (res.data && res.data.layoutJson) {
      try {
        const layout = typeof res.data.layoutJson === 'string'
          ? JSON.parse(res.data.layoutJson)
          : (res.data.layoutJson || []);
        pageElements.value = Array.isArray(layout) ? layout : [];
      } catch { pageElements.value = []; }
    }
  } catch {
    pageElements.value = [];
  }
}

// ===== 导航 =====
function goHome() {
  router.push('/home');
}

function goDesign(page: any) {
  if (page?.id) {
    router.push(`/lowcode/page/design?id=${page.id}`);
  }
}

onMounted(() => {
  loadData();
  updateTime();
  timeTimer = setInterval(updateTime, 30000);
});

onUnmounted(() => {
  if (timeTimer) clearInterval(timeTimer);
});
</script>

<style scoped lang="less">
// ===== 整体布局 =====
.app-runtime {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: #f0f2f5;
}

// ===== 左侧导航栏 =====
.ar-sidebar {
  width: 240px;
  min-width: 240px;
  height: 100vh;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.04);
  z-index: 10;
}

.ar-sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 18px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.ar-brand-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.ar-brand-info {
  flex: 1;
  min-width: 0;
  h3 {
    font-size: 16px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

// 导航菜单
.ar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 12px 8px;
}

.ar-nav-label {
  font-size: 11px;
  font-weight: 600;
  color: #bbb;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 8px 12px 6px;
}

.ar-nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  margin: 2px 0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
  color: #595959;
  font-size: 14px;
  position: relative;

  &:hover {
    background: #f5f7fa;
    color: #1a1a2e;
  }

  &.active {
    background: linear-gradient(135deg, #eff6ff, #f0f5ff);
    color: #3b82f6;
    font-weight: 600;

    .ar-nav-icon {
      color: #3b82f6;
    }
  }
}

.ar-nav-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #d9d9d9;
  flex-shrink: 0;
  transition: all 0.2s;

  &.on {
    background: #3b82f6;
    box-shadow: 0 0 6px rgba(59, 130, 246, 0.4);
  }
}

.ar-nav-icon {
  flex-shrink: 0;
  color: #8c8c8c;
}

.ar-nav-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ar-nav-badge {
  flex-shrink: 0;
}

.ar-nav-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 32px 16px;
  color: #ccc;
  p { font-size: 13px; color: #999; margin: 0; }
}

// 底部
.ar-sidebar-footer {
  padding: 12px;
  border-top: 1px solid #f0f0f0;
}

// ===== 主内容区 =====
.ar-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 顶部栏
.ar-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  height: 52px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.ar-topbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8c8c8c;
}

.ar-breadcrumb-app {
  font-size: 14px;
  color: #8c8c8c;
  cursor: pointer;

  &:hover {
    color: #3b82f6;
  }
}

.ar-breadcrumb-sep {
  color: #d9d9d9;
}

.ar-breadcrumb-page {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.ar-topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.ar-time {
  font-size: 12px;
  color: #bbb;
  font-variant-numeric: tabular-nums;
}

// 内容区
.ar-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.ar-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

// 欢迎页
.ar-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  text-align: center;
}

.ar-welcome-icon {
  width: 88px;
  height: 88px;
  border-radius: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.ar-welcome h2 {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.ar-welcome-desc {
  font-size: 15px;
  color: #8c8c8c;
  margin: 0 0 32px;
}

.ar-welcome-stats {
  display: flex;
  gap: 40px;
  margin-bottom: 36px;
}

.ar-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.ar-stat-num {
  font-size: 32px;
  font-weight: 700;
  color: #3b82f6;
  line-height: 1;
}

.ar-stat-label {
  font-size: 13px;
  color: #8c8c8c;
}

.ar-welcome-nav {
  p {
    font-size: 14px;
    color: #999;
    margin: 0 0 16px;
  }
}

.ar-shortcut-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 10px;
  max-width: 600px;
}

.ar-shortcut-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  color: #595959;

  &:hover {
    border-color: #3b82f6;
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.08);
    color: #3b82f6;
    transform: translateY(-1px);
  }
}

.ar-shortcut-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
  flex-shrink: 0;
}

.ar-shortcut-arrow {
  color: #d9d9d9;
  margin-left: auto;
}

// 页面渲染区
.ar-page-viewport {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  min-height: 500px;
  padding: 24px;
}

.ar-page-content {
  position: relative;
  min-height: 400px;
}

.page-element {
  position: absolute;
  overflow: hidden;
}

.ar-empty-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #ccc;
  gap: 8px;

  h3 { font-size: 18px; color: #aaa; margin: 0; }
  p { font-size: 14px; color: #bbb; margin: 0 0 12px; }
}
</style>
