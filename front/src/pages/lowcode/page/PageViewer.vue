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
          <!-- 按类型渲染每个元素 -->
          <div v-for="element in pageElements" :key="element.id" class="page-element">
            <template v-if="element.type === 'heading'">
              <component :is="`h${element.props?.level || 2}`" :style="getElementStyle(element)">
                {{ element.props?.text || '标题' }}
              </component>
            </template>
            <template v-else-if="element.type === 'text'">
              <p :style="getElementStyle(element)">{{ element.props?.content || '文本内容' }}</p>
            </template>
            <template v-else-if="element.type === 'button'">
              <t-button :theme="element.props?.variant || 'primary'" :size="element.props?.size || 'medium'">
                {{ element.props?.text || '按钮' }}
              </t-button>
            </template>
            <template v-else-if="element.type === 'input'">
              <t-input
                :placeholder="element.props?.placeholder || '请输入'"
                :style="getElementStyle(element)"
              />
            </template>
            <template v-else-if="element.type === 'image'">
              <div class="image-placeholder" :style="getElementStyle(element)">
                <ImageIcon size="32" />
                <span>图片</span>
              </div>
            </template>
            <template v-else-if="element.type === 'table'">
              <t-table
                :columns="element.props?.columns || [{ colKey: 'col1', title: '列1' }]"
                :data="[]"
                bordered
                stripe
                :style="getElementStyle(element)"
              />
            </template>
            <template v-else-if="element.type === 'card'">
              <t-card :title="element.props?.title || '卡片'" :style="getElementStyle(element)">
                {{ element.props?.content || '卡片内容' }}
              </t-card>
            </template>
            <template v-else>
              <div class="fallback-element" :style="getElementStyle(element)">
                {{ element.type }} 组件
              </div>
            </template>
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
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import {
  BrowseIcon, FullscreenIcon, FullscreenExitIcon,
  ImageIcon, LayoutIcon
} from 'tdesign-icons-vue-next';
import BackButton from '../../../components/common/BackButton.vue';
import { pageSchemaApi } from '../../../api/lowcode/pageSchema';

const router = useRouter();
const route = useRoute();

const pageData = ref<any>(null);
const pageElements = ref<any[]>([]);
const loading = ref(true);
const isFullscreen = ref(false);

onMounted(async () => {
  const code = route.query.code as string;
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

function getElementStyle(element: any) {
  const props = element.props || {};
  const style: any = {};
  if (props.width) style.width = typeof props.width === 'number' ? `${props.width}px` : props.width;
  if (props.color) style.color = props.color;
  if (props.backgroundColor) style.backgroundColor = props.backgroundColor;
  if (props.align) style.textAlign = props.align;
  return style;
}

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
  min-height: 400px;
}

.page-element {
  margin-bottom: 16px;
}

.image-placeholder {
  background: #f5f5f5;
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px;
  color: #ccc;
  font-size: 14px;
}

.fallback-element {
  padding: 20px;
  background: #fafafa;
  border: 1px dashed #e5e5e5;
  border-radius: 8px;
  text-align: center;
  color: #999;
  font-size: 13px;
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
