<template>
  <!-- 全应用列表模式 -->
  <div v-if="isAllMode" class="app-detail-page">
    <div class="ad-header">
      <t-button variant="outline" @click="router.push('/home')">
        <template #icon><ChevronLeftIcon size="16" /></template>
        返回主页
      </t-button>
      <h2 class="ad-all-title">业务应用</h2>
    </div>
    <div class="ad-content">
      <div v-if="allApps.length > 0" class="all-apps-grid">
        <div
          v-for="a in allApps"
          :key="a.code"
          class="all-app-card"
          @click="goToApp(a)"
        >
          <div class="all-app-icon" :style="{ background: a.color || 'linear-gradient(135deg, #f5a623, #e8a317)' }">
            <AppIcon size="24" />
          </div>
          <div class="all-app-info">
            <h4>{{ a.name }}</h4>
            <p>{{ a.description || '暂无描述' }}</p>
          </div>
          <div class="all-app-meta">
            <span>{{ (a as any).pageCount ?? 0 }} 个页面</span>
            <div class="all-app-actions" @click.stop>
              <t-button
                size="small"
                variant="text"
                theme="primary"
                title="编辑该应用的菜单"
                @click.stop="goEditMenu(a)"
              >
                <template #icon><SettingIcon size="14" /></template>
                菜单
              </t-button>
              <ChevronRightIcon size="16" />
            </div>
          </div>
        </div>
      </div>
      <div v-else class="ad-empty">
        <AppIcon size="48" />
        <p>暂无业务应用</p>
        <t-button theme="primary" @click="$router.push('/lowcode/page/list')">去创建页面并发布</t-button>
      </div>
    </div>
  </div>

  <!-- 单应用详情模式 -> 重定向到运行时 -->
  <div v-else class="app-detail-page">
    <div class="ad-loading">
      <t-loading size="large" text="正在进入应用..." />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { businessAppApi, type BusinessApp } from '../../../api/lowcode/businessApp';
import { AppIcon, ChevronRightIcon, ChevronLeftIcon, SettingIcon } from 'tdesign-icons-vue-next';

const route = useRoute();
const router = useRouter();

const appCode = (route.params.appCode as string) || '';
const allApps = ref<BusinessApp[]>([]);
const isAllMode = !appCode;

async function loadData() {
  if (isAllMode) {
    await loadAllApps();
  } else {
    router.replace(`/run/${appCode}`);
  }
}

async function loadAllApps() {
  try {
    const res = await businessAppApi.getAll();
    allApps.value = (res.data as any)?.data || res.data || [];
    for (const a of allApps.value) {
      try {
        const pr = await businessAppApi.getPages(a.code);
        const pd = (pr.data as any)?.data || pr.data || [];
        (a as any).pageCount = Array.isArray(pd) ? pd.length : 0;
      } catch {
        (a as any).pageCount = 0;
      }
    }
  } catch {
    allApps.value = [];
  }
}

function goToApp(app: BusinessApp) {
  router.push(`/run/${app.code}`);
}

function goEditMenu(app: BusinessApp) {
  router.push(`/lowcode/app/${app.code}/menu`);
}

onMounted(loadData);
</script>

<style scoped lang="less">
.app-detail-page {
  min-height: calc(100vh - 64px);
  background: #f7f8fa;
  padding: 24px;
}
.ad-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}
.ad-content {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}
.ad-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}
.ad-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px;
  color: #ccc;
  p { font-size: 14px; color: #999; margin: 0; }
}

.ad-all-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.all-apps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 14px;
}

.all-app-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border: 1px solid #f0f0f0;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
    border-color: #e8a317;
  }
}

.all-app-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.all-app-info {
  flex: 1;
  min-width: 0;
  h4 {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0 0 4px;
  }
  p {
    font-size: 13px;
    color: #999;
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.all-app-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  color: #ccc;
  span {
    font-size: 12px;
    color: #999;
  }
}

.all-app-actions {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
</style>
