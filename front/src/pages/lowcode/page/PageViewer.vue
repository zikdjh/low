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
          <SchemaRenderer :elements="pageElements" :enable-events="true">
            <template #empty>
              <div class="empty-page-hint">
                <LayoutIcon size="64" />
                <h3>空白页面</h3>
                <p>该页面尚未添加任何组件</p>
              </div>
            </template>
          </SchemaRenderer>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import {
  BrowseIcon, FullscreenIcon, FullscreenExitIcon, LayoutIcon,
} from 'tdesign-icons-vue-next';
import BackButton from '../../../components/common/BackButton.vue';
import SchemaRenderer from '../../../components/lowcode/SchemaRenderer.vue';
import { pageSchemaApi } from '../../../api/lowcode/pageSchema';

const router = useRouter();
const route = useRoute();

const pageData = ref<any>(null);
const pageElements = ref<any[]>([]);
const loading = ref(true);
const isFullscreen = ref(false);

async function loadPage() {
  loading.value = true;
  const code = (route.query.code as string)
    || (route.params.pageCode as string)
    || (route.meta.pageCode as string);
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
}

onMounted(() => {
  loadPage();
});

// 监听路由 pageCode（query.code 或 meta.pageCode）变化重新加载
// 不再硬编码 /leave/ 前缀，新版业务应用走 /run/:appCode/:pageCode 也能复用
watch(
  () => [route.query.code, route.params.pageCode, route.meta.pageCode],
  (next, prev) => {
    if (next.join('|') !== (prev || []).join('|')) {
      loadPage();
    }
  },
);

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
