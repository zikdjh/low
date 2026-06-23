<template>
  <div class="page-viewer">
    <!-- 顶部工具栏 -->
    <div class="viewer-header">
      <div class="viewer-header-left">
        <BackButton to="/lowcode/page/list" label="返回页面列表" />
        <span class="viewer-title">{{ pageData?.name || '页面预览' }}</span>
        <t-tag v-if="pageData?.status === 'published'" theme="success" variant="light">已发布</t-tag>
        <t-tag v-else theme="warning" variant="light">草稿</t-tag>
      </div>
      <div class="viewer-header-right">
        <t-button variant="outline" @click="isFullscreen = !isFullscreen">
          <template #icon><FullscreenIcon v-if="!isFullscreen" /><FullscreenExitIcon v-else /></template>
          {{ isFullscreen ? '退出全屏' : '全屏' }}
        </t-button>
      </div>
    </div>

    <!-- 预览区域 -->
    <div class="viewer-container" :class="{ fullscreen: isFullscreen }">
      <div v-if="loading" class="viewer-loading">
        <t-loading size="large" text="加载中..." />
      </div>
      <div v-else-if="!pageData" class="viewer-empty">
        <BrowseIcon size="48" />
        <h3>页面不存在</h3>
        <p>未找到该页面，请检查页面编码是否正确</p>
        <t-button theme="primary" @click="goBack">返回</t-button>
      </div>
      <div v-else class="viewer-frame">
        <div class="viewer-page-content">
          <!-- 按类型渲染每个元素，使用组件渲染器并启用事件 -->
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
          <div v-if="pageElements.length === 0" class="empty-page-hint">
            <LayoutIcon size="64" />
            <h3>空白页面</h3>
            <p>该页面尚未添加任何组件</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, markRaw } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import {
  BrowseIcon, FullscreenIcon, FullscreenExitIcon,
  ImageIcon, LayoutIcon, EditIcon, ButtonIcon, LinkIcon, TapeIcon,
  FormIcon, TextboxIcon, CalendarIcon, TimeIcon, CheckCircleFilledIcon,
  CircleIcon, StarFilledIcon, ControlPlatformIcon, AddCircleIcon,
  ChevronDownIcon, ChevronUpIcon, LayersIcon, CodeIcon, FrameIcon,
  FolderIcon, RootListIcon, SwapIcon, UploadIcon, SettingIcon,
} from 'tdesign-icons-vue-next';
import BackButton from '../../../components/common/BackButton.vue';
import { pageSchemaApi } from '../../../api/lowcode/pageSchema';

const router = useRouter();
const route = useRoute();

const pageData = ref<any>(null);
const pageElements = ref<any[]>([]);
const loading = ref(true);
const isFullscreen = ref(false);

// 组件映射表
const componentMap: Record<string, any> = {
  text: () => import('./components/TextElement.vue'),
  button: () => import('./components/ButtonElement.vue'),
  link: () => import('./components/LinkElement.vue'),
  image: () => import('./components/ImageElement.vue'),
  input: () => import('./components/InputElement.vue'),
  textarea: () => import('./components/TextareaElement.vue'),
  inputNumber: () => import('./components/InputNumberElement.vue'),
  select: () => import('./components/SelectElement.vue'),
  date: () => import('./components/DateElement.vue'),
  time: () => import('./components/TimeElement.vue'),
  switch: () => import('./components/SwitchElement.vue'),
  checkbox: () => import('./components/CheckboxElement.vue'),
  radio: () => import('./components/RadioElement.vue'),
  slider: () => import('./components/SliderElement.vue'),
  rate: () => import('./components/RateElement.vue'),
  upload: () => import('./components/UploadElement.vue'),
  table: () => import('./components/TableElement.vue'),
  form: () => import('./components/FormElement.vue'),
  list: () => import('./components/ListElement.vue'),
  chart: () => import('./components/ChartElement.vue'),
  card: () => import('./components/CardElement.vue'),
  tag: () => import('./components/TagElement.vue'),
  progress: () => import('./components/ProgressElement.vue'),
  steps: () => import('./components/StepsElement.vue'),
  alert: () => import('./components/AlertElement.vue'),
  divider: () => import('./components/DividerElement.vue'),
  container: () => import('./components/ContainerElement.vue'),
  grid: () => import('./components/GridElement.vue'),
  tabs: () => import('./components/TabsElement.vue'),
  collapse: () => import('./components/CollapseElement.vue'),
  space: () => import('./components/SpaceElement.vue'),
  breadcrumb: () => import('./components/BreadcrumbElement.vue'),
};

import { defineAsyncComponent } from 'vue';

function getElementComponent(type: string) {
  const loader = componentMap[type];
  if (loader) {
    return defineAsyncComponent(loader);
  }
  return null;
}

onMounted(async () => {
  const code = (route.query.code as string) || (route.meta.pageCode as string);
  if (!code) {
    loading.value = false;
    return;
  }
  try {
    const res = await pageSchemaApi.getByPageCode(code);
    if (res.data.code === 1 && res.data.data) {
      pageData.value = res.data.data;
      try {
        const layout = typeof res.data.data.layoutJson === 'string'
          ? JSON.parse(res.data.data.layoutJson)
          : (res.data.data.layoutJson || []);
        pageElements.value = Array.isArray(layout) ? layout : [];
      } catch { }
    }
  } catch (e: any) {
    MessagePlugin.error('加载页面失败');
  } finally {
    loading.value = false;
  }
});

function goBack() {
  router.push('/lowcode/page/list');
}
</script>

<style scoped lang="less">
.page-viewer {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f7f8fa;
}

.viewer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
  .viewer-header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .viewer-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
  }
}

.viewer-container {
  flex: 1;
  overflow: auto;
  display: flex;
  justify-content: center;
  padding: 32px;
  &.fullscreen {
    position: fixed;
    inset: 0;
    z-index: 9999;
    background: #fff;
    padding: 16px;
  }
}

.viewer-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.viewer-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
  gap: 12px;
  h3 { font-size: 18px; color: #666; margin: 0; }
  p { font-size: 14px; margin: 0; }
}

.viewer-frame {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  width: 100%;
  max-width: 1200px;
  min-height: 600px;
  padding: 32px;
}

.viewer-page-content {
  position: relative;
  min-height: 400px;
}

.page-element {
  position: absolute;
  overflow: hidden;
}

.empty-page-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #ccc;
  gap: 8px;
  h3 { font-size: 18px; color: #aaa; margin: 0; }
  p { font-size: 14px; color: #bbb; margin: 0; }
}
</style>
