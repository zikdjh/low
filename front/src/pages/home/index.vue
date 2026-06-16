<template>
  <div class="home-dashboard">
    <!-- 横幅 -->
    <div class="welcome-hero">
      <div class="hero-bg-decor">
        <div class="decor-circle c1"></div>
        <div class="decor-circle c2"></div>
        <div class="decor-circle c3"></div>
      </div>
      <div class="hero-content">
        <div class="hero-text">
          <h1>欢迎回来，<em>{{ userName }}</em></h1>
          <p>快速构建企业级应用 — 低代码让开发更高效</p>
          <div class="hero-cta">
            <t-button theme="primary" size="large" @click="$router.push('/lowcode/page/list')">
              <template #icon><AddCircleIcon /></template>
              新建页面
            </t-button>
            <t-button theme="primary" size="large" @click="$router.push('/lowcode/entity')">
              <template #icon><DataBaseIcon /></template>
              管理实体
            </t-button>
          </div>
        </div>
        <div class="hero-visual">
          <svg viewBox="0 0 260 200" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="20" y="20" width="100" height="72" rx="12" fill="#fff" fill-opacity="0.18" />
            <rect x="30" y="34" width="50" height="8" rx="4" fill="#fff" fill-opacity="0.35" />
            <rect x="30" y="48" width="80" height="6" rx="3" fill="#fff" fill-opacity="0.25" />
            <rect x="30" y="60" width="60" height="6" rx="3" fill="#fff" fill-opacity="0.25" />
            <rect x="130" y="20" width="110" height="72" rx="12" fill="#fff" fill-opacity="0.12" />
            <rect x="142" y="34" width="86" height="6" rx="3" fill="#fff" fill-opacity="0.2" />
            <rect x="142" y="46" width="60" height="6" rx="3" fill="#fff" fill-opacity="0.15" />
            <circle cx="220" cy="56" r="12" fill="#fff" fill-opacity="0.2" />
            <rect x="20" y="104" width="220" height="48" rx="12" fill="#fff" fill-opacity="0.1" />
            <rect x="34" y="118" width="120" height="6" rx="3" fill="#fff" fill-opacity="0.18" />
            <rect x="34" y="130" width="80" height="6" rx="3" fill="#fff" fill-opacity="0.15" />
            <circle cx="220" cy="128" r="14" fill="#fff" fill-opacity="0.13" />
          </svg>
        </div>
      </div>
    </div>

    <!-- 统计卡片行 -->
    <div class="stat-row">
      <div v-for="card in statCards" :key="card.key" class="stat-card" @click="card.link && $router.push(card.link)">
        <div class="stat-icon-box" :style="{ background: card.gradient }">
          <component :is="card.icon" size="24" />
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ card.value.toLocaleString() }}</div>
          <div class="stat-desc">{{ card.label }}</div>
        </div>
        <div class="stat-trend" :class="card.trendDir">
          <component :is="card.trendIcon" size="12" />
          <span>{{ card.trend }}</span>
        </div>
      </div>
    </div>

    <!-- 双栏布局 -->
    <div class="two-col">
      <!-- 快捷导航 -->
      <div class="panel quick-nav-panel">
        <div class="panel-header">
          <h3><RocketIcon size="18" /> 快捷操作</h3>
        </div>
        <div class="nav-grid">
          <div v-for="item in quickNavs" :key="item.label" class="nav-item" @click="item.action">
            <div class="nav-icon" :style="{ background: item.bgColor }">
              <component :is="item.icon" size="20" />
            </div>
            <div class="nav-text">
              <span>{{ item.label }}</span>
              <small>{{ item.desc }}</small>
            </div>
          </div>
        </div>
      </div>

      <!-- 实体统计面板 -->
      <div class="panel entity-panel">
        <div class="panel-header">
          <h3><ChartIcon size="18" /> 数据概览</h3>
          <t-button variant="text" size="small" @click="$router.push('/lowcode/entity')">查看全部 →</t-button>
        </div>
        <div class="entity-list-mini" v-if="topEntities.length > 0">
          <div v-for="entity in topEntities" :key="entity.id" class="entity-row" @click="goToEntityData(entity)">
            <div class="entity-row-icon">
              <TableIcon size="16" />
            </div>
            <div class="entity-row-info">
              <span class="entity-name">{{ entity.name }}</span>
              <span class="entity-code">{{ entity.code }}</span>
            </div>
            <t-tag :theme="entity.status === 'published' ? 'success' : 'warning'" variant="light" size="small">
              {{ entity.status === 'published' ? '已发布' : '草稿' }}
            </t-tag>
          </div>
        </div>
        <div v-else class="panel-empty">
          <DataBaseIcon size="32" />
          <p>暂无实体数据</p>
          <t-button size="small" theme="primary" @click="$router.push('/lowcode/entity')">创建实体</t-button>
        </div>
      </div>
    </div>

    <!-- 底部能力卡片 -->
    <div class="capability-section">
      <h3 class="capability-title"><StarIcon size="18" /> 平台能力</h3>
      <div class="cap-grid">
        <div v-for="cap in capabilities" :key="cap.title" class="cap-card">
          <div class="cap-icon" :style="{ background: cap.gradient }">
            <component :is="cap.icon" size="28" />
          </div>
          <h4>{{ cap.title }}</h4>
          <p>{{ cap.desc }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import {
  AddCircleIcon, DataBaseIcon, LayoutIcon, TableIcon,
  StarIcon, ChartIcon, TrendingUpIcon, TrendingDownIcon,
  CodeIcon, ShieldErrorIcon, FileIcon, SettingIcon, LightbulbIcon,
  RocketIcon
} from 'tdesign-icons-vue-next';
import entityMetaApi from '../../api/lowcode/entityMeta';
import { pageSchemaApi } from '../../api/lowcode/pageSchema';
import { useUserStore } from '../../store';

const router = useRouter();
const userStore = useUserStore();
const userName = ref(userStore.displayName || '管理员');

// 统计数据
const statCards = ref([
  { key: 'entities', icon: DataBaseIcon, value: 0, label: '实体总数', trend: '--', trendDir: 'up', trendIcon: TrendingUpIcon, gradient: 'linear-gradient(135deg, #f5a623, #e8a317)', link: '/lowcode/entity' },
  { key: 'pages', icon: LayoutIcon, value: 0, label: '页面数量', trend: '--', trendDir: 'up', trendIcon: TrendingUpIcon, gradient: 'linear-gradient(135deg, #6366f1, #8b5cf6)', link: '/lowcode/page/list' },
  { key: 'published', icon: RocketIcon, value: 0, label: '已发布数', trend: '--', trendDir: 'up', trendIcon: TrendingUpIcon, gradient: 'linear-gradient(135deg, #10b981, #34d399)', link: null },
  { key: 'drafts', icon: FileIcon, value: 0, label: '草稿数', trend: '--', trendDir: 'down', trendIcon: TrendingDownIcon, gradient: 'linear-gradient(135deg, #06b6d4, #22d3ee)', link: null },
]);

const topEntities = ref<any[]>([]);

// 快捷导航
const quickNavs = [
  { icon: DataBaseIcon, label: '创建实体', desc: '定义数据模型', bgColor: 'linear-gradient(135deg, #f5a623, #e8a317)', action: () => router.push('/lowcode/entity') },
  { icon: LayoutIcon, label: '设计页面', desc: '可视化拖拽', bgColor: 'linear-gradient(135deg, #6366f1, #8b5cf6)', action: () => router.push('/lowcode/page/list') },
  { icon: SettingIcon, label: '系统管理', desc: '配置与设置', bgColor: 'linear-gradient(135deg, #06b6d4, #22d3ee)', action: () => router.push('/lowcode/entity') },
  { icon: LightbulbIcon, label: '使用帮助', desc: '操作指南', bgColor: 'linear-gradient(135deg, #10b981, #34d399)', action: () => MessagePlugin.info('功能开发中...') },
];

// 平台能力
const capabilities = [
  { icon: LayoutIcon, title: '可视化设计', desc: '拖拽式组件设计，所见即所得', gradient: 'linear-gradient(135deg, #f5a623, #e8a317)' },
  { icon: DataBaseIcon, title: '数据模型', desc: '灵活定义实体，自动生成API', gradient: 'linear-gradient(135deg, #6366f1, #8b5cf6)' },
  { icon: CodeIcon, title: '代码生成', desc: '一键生成前后端代码', gradient: 'linear-gradient(135deg, #06b6d4, #22d3ee)' },
  { icon: ShieldErrorIcon, title: '安全可靠', desc: '内置权限管理与数据校验', gradient: 'linear-gradient(135deg, #10b981, #34d399)' },
];

function goToEntityData(entity: any) {
  if (entity.code) {
    router.push(`/lowcode/data/${entity.code}`);
  }
}

async function loadData() {
  try {
    // 加载实体统计
    const entityRes = await entityMetaApi.list({ page: 1, pageSize: 100 });
    if (entityRes.data.code === 1) {
      const entityData = entityRes.data.data;
      const records = entityData.records || entityData.content || [];
      const total = entityData.totalElements || entityData.total || records.length;
      const publishedCount = records.filter((e: any) => e.status === 'published').length;

      statCards.value[0].value = total;
      statCards.value[0].trend = total > 0 ? `+${total}` : '--';
      statCards.value[2].value = publishedCount;
      statCards.value[2].trend = publishedCount > 0 ? `+${publishedCount}` : '--';
      statCards.value[3].value = total - publishedCount;
      statCards.value[3].trendDir = total - publishedCount > 0 ? 'down' : 'up';
      statCards.value[3].trend = total - publishedCount > 0 ? `${total - publishedCount}` : '--';

      topEntities.value = records.slice(0, 5);
    }

    // 加载页面统计
    try {
      const pageRes = await pageSchemaApi.getAllPages();
      if (pageRes.data.code === 1) {
        const pageData = pageRes.data.data;
        const pages = Array.isArray(pageData) ? pageData : (pageData || []);
        statCards.value[1].value = pages.length;
        statCards.value[1].trend = pages.length > 0 ? `+${pages.length}` : '--';
      }
    } catch {
      statCards.value[1].value = 0;
    }
  } catch {
    // 使用默认值
  }
}

onMounted(() => {
  loadData();
});
</script>

<style scoped lang="less">
.home-dashboard {
  padding: 28px;
  max-width: 1400px;
  margin: 0 auto;
}

/* ===== 欢迎横幅 ===== */
.welcome-hero {
  position: relative;
  background: linear-gradient(135deg, #1a1a2e 0%, #1e1e3a 30%, #2d2d4a 60%, #1a1a2e 100%);
  border-radius: 20px;
  padding: 48px 56px;
  margin-bottom: 28px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.04);
}
.hero-bg-decor {
  position: absolute;
  inset: 0;
  pointer-events: none;
  .decor-circle {
    position: absolute;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba(245, 166, 35, 0.2), rgba(232, 163, 23, 0.05));
  }
  .c1 { width: 320px; height: 320px; top: -80px; right: -60px; }
  .c2 { width: 180px; height: 180px; bottom: -40px; left: 10%; }
  .c3 { width: 60px; height: 60px; top: 30%; right: 40%; opacity: 0.4; }
}
.hero-content {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 2;
}
.hero-text {
  flex: 1;
  h1 {
    font-size: 30px;
    font-weight: 700;
    color: #fff;
    margin: 0 0 12px;
    line-height: 1.35;
    em {
      font-style: normal;
      color: #f5a623;
      border-bottom: 2px solid rgba(245, 166, 35, 0.4);
      padding-bottom: 2px;
    }
  }
  p {
    font-size: 15px;
    color: rgba(255, 255, 255, 0.65);
    margin: 0 0 28px;
  }
}
.hero-cta {
  display: flex;
  gap: 12px;
}
.hero-visual {
  flex-shrink: 0;
  margin-left: 40px;
  svg { width: 260px; height: auto; }
}

/* ===== 统计卡片 ===== */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}
.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.25s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  }
}
.stat-icon-box {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.stat-info { flex: 1; min-width: 0; }
.stat-number {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}
.stat-desc {
  font-size: 13px;
  color: #8f959e;
  margin-top: 2px;
}
.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
  flex-shrink: 0;
  &.up { color: #10b981; background: #ecfdf5; }
  &.down { color: #ef4444; background: #fef2f2; }
}

/* ===== 双栏 ===== */
.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 28px;
}
.panel {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  h3 {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0;
  }
}

/* 快捷操作 */
.nav-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #f5f5f5;
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
    border-color: transparent;
  }
}
.nav-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.nav-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  span { font-size: 14px; font-weight: 600; color: #1a1a1a; }
  small { font-size: 12px; color: #999; }
}

/* 实体列表迷你 */
.entity-list-mini {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.entity-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
  &:hover { background: #fafafa; }
}
.entity-row-icon {
  width: 36px;
  height: 36px;
  background: #f5f5f5;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  flex-shrink: 0;
}
.entity-row-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  .entity-name { font-size: 14px; font-weight: 600; color: #1a1a1a; }
  .entity-code { font-size: 12px; color: #999; font-family: 'JetBrains Mono', monospace; }
}
.panel-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 32px;
  color: #ccc;
  p { font-size: 14px; color: #999; margin: 0; }
}

/* ===== 平台能力 ===== */
.capability-section {
  margin-bottom: 32px;
}
.capability-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 20px;
}
.cap-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.cap-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px 24px;
  text-align: center;
  transition: all 0.3s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
  }
}
.cap-icon {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin: 0 auto 16px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}
.cap-card h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}
.cap-card p {
  font-size: 13px;
  color: #8f959e;
  margin: 0;
  line-height: 1.6;
}

/* ===== 响应式 ===== */
@media (max-width: 1200px) {
  .stat-row { grid-template-columns: repeat(2, 1fr); }
  .two-col { grid-template-columns: 1fr; }
  .cap-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .home-dashboard { padding: 16px; }
  .welcome-hero { padding: 32px 24px; }
  .hero-content { flex-direction: column; text-align: center; }
  .hero-visual { margin: 24px 0 0; }
  .hero-text h1 { font-size: 24px; }
}
</style>
