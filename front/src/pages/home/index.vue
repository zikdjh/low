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
            <t-button theme="primary" size="large" @click="$router.push('/lowcode/app')">
              <template #icon><AppIcon /></template>
              业务应用
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

    <!-- 业务应用卡片 -->
    <div class="app-section" v-if="businessApps.length > 0">
      <div class="app-header">
        <h3><RocketIcon size="18" /> 业务应用</h3>
        <t-button variant="text" size="small" @click="$router.push('/lowcode/app')">查看全部 →</t-button>
      </div>
      <div class="app-grid">
        <div
          v-for="app in businessApps"
          :key="app.code"
          class="app-card"
          @click="goToApp(app)"
        >
          <div class="app-card-icon" :style="{ background: app.color || 'linear-gradient(135deg, #f5a623, #e8a317)' }">
            <component :is="getAppIcon(app.icon)" size="22" />
          </div>
          <div class="app-card-info">
            <h4>{{ app.name }}</h4>
            <p>{{ app.description || '暂无描述' }}</p>
          </div>
          <div class="app-card-arrow">
            <ChevronRightIcon size="16" />
          </div>
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

  <!-- 使用帮助弹窗 -->
  <t-dialog
    v-model:visible="showHelpDialog"
    header="使用帮助"
    width="900px"
    :top="30"
    :footer="false"
    :close-on-overlay-click="true"
    class="help-dialog"
  >
    <div class="help-dialog-content">
      <!-- 顶部介绍 -->
      <div class="help-intro">
        <h3>📖 欢迎使用低代码开发平台</h3>
        <p>本平台提供可视化页面设计、数据建模、权限管理等能力，帮助您快速构建企业级应用。以下是各模块的详细使用说明：</p>
      </div>

      <!-- 双列卡片布局 -->
      <div class="help-card-grid">
        <!-- 页面设计 -->
        <div class="help-card" style="--card-color: #6366f1">
          <div class="card-header">
            <div class="card-icon"><LayoutIcon size="22" /></div>
            <span>页面设计</span>
          </div>
          <div class="card-body">
            <div class="step-item">
              <span class="step-num">1</span>
              <div class="step-text">
                <b>新建页面</b>
                <p>进入「设计页面」列表，点击新建按钮，输入页面名称和编码</p>
              </div>
            </div>
            <div class="step-item">
              <span class="step-num">2</span>
              <div class="step-text">
                <b>拖拽组件</b>
                <p>从左侧组件库拖拽按钮、输入框、表格等组件到画布中，自由调整位置和尺寸</p>
              </div>
            </div>
            <div class="step-item">
              <span class="step-num">3</span>
              <div class="step-text">
                <b>配置属性</b>
                <p>选中组件后在右侧面板设置样式、事件和实体数据绑定</p>
              </div>
            </div>
            <div class="step-item">
              <span class="step-num">4</span>
              <div class="step-text">
                <b>预览发布</b>
                <p>点击「预览」查看效果，「保存」后发布上线</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 数据建模 -->
        <div class="help-card" style="--card-color: #f5a623">
          <div class="card-header">
            <div class="card-icon"><DataBaseIcon size="22" /></div>
            <span>数据建模</span>
          </div>
          <div class="card-body">
            <div class="step-item">
              <span class="step-num">1</span>
              <div class="step-text">
                <b>创建实体</b>
                <p>进入「创建实体」页面，定义表名、中文名和字段列表</p>
              </div>
            </div>
            <div class="step-item">
              <span class="step-num">2</span>
              <div class="step-text">
                <b>定义字段</b>
                <p>支持文本、数字、日期、关联实体等多种字段类型，可设置必填、默认值等</p>
              </div>
            </div>
            <div class="step-item">
              <span class="step-num">3</span>
              <div class="step-text">
                <b>自动生成</b>
                <p>创建实体后系统自动生成数据库表和 CRUD 接口，无需手动编写代码</p>
              </div>
            </div>
            <div class="step-item">
              <span class="step-num">4</span>
              <div class="step-text">
                <b>绑定页面</b>
                <p>在页面设计器中拖入业务实体组件，表格/表单自动加载字段定义</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 系统设置 -->
        <div class="help-card" style="--card-color: #06b6d4">
          <div class="card-header">
            <div class="card-icon"><SettingIcon size="22" /></div>
            <span>系统设置</span>
          </div>
          <div class="card-body">
            <ul class="feature-list">
              <li>🎨 <b>主题切换</b> — 支持亮色/暗色模式，可自定义品牌色</li>
              <li>📐 <b>布局模式</b> — 侧边栏展开/折叠，顶部/侧边导航切换</li>
              <li>🔔 <b>通知管理</b> — 配置站内消息提醒和邮件通知</li>
              <li>👤 <b>账户管理</b> — 修改个人密码、头像和基础信息</li>
              <li>🛡️ <b>权限控制</b> — 管理员可分配角色和操作权限</li>
            </ul>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="help-card" style="--card-color: #10b981">
          <div class="card-header">
            <div class="card-icon"><RocketIcon size="22" /></div>
            <span>快捷技巧</span>
          </div>
          <div class="card-body">
            <div class="tip-row">
              <kbd>Ctrl + K</kbd>
              <span>打开命令面板，全局搜索页面/实体/功能</span>
            </div>
            <div class="tip-row">
              <kbd>Ctrl + S</kbd>
              <span>在页面设计器中快速保存当前页面</span>
            </div>
            <div class="tip-row">
              <kbd>拖拽空白区</kbd>
              <span>在设计器画布空白区域按住鼠标拖动可平移视图</span>
            </div>
            <div class="tip-row">
              <kbd>右键组件</kbd>
              <span>在画布中右键组件可快速复制、删除或置顶</span>
            </div>
            <div class="tip-row">
              <kbd>实体绑定</kbd>
              <span>表格/表单选中实体后自动加载字段，无需手动配列</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部提示 -->
      <div class="help-footer">
        <LightbulbIcon size="16" />
        <span>提示：更多高级功能请查阅 <b>API 文档</b> 或联系系统管理员</span>
      </div>
    </div>
  </t-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import {
  AddCircleIcon, DataBaseIcon, LayoutIcon, TableIcon,
  StarIcon, ChartIcon, TrendingUpIcon, TrendingDownIcon,
  CodeIcon, ShieldErrorIcon, FileIcon, SettingIcon, LightbulbIcon,
  RocketIcon, AppIcon, ChevronRightIcon, CalendarIcon
} from 'tdesign-icons-vue-next';
import entityMetaApi from '../../api/lowcode/entityMeta';
import { pageSchemaApi } from '../../api/lowcode/pageSchema';
import { businessAppApi, type BusinessApp } from '../../api/lowcode/businessApp';
import { useUserStore } from '../../store';

const router = useRouter();
const userStore = useUserStore();
const userName = ref(userStore.displayName || '管理员');
const showHelpDialog = ref(false);

// 统计数据
const statCards = ref([
  { key: 'entities', icon: DataBaseIcon, value: 0, label: '实体总数', trend: '--', trendDir: 'up', trendIcon: TrendingUpIcon, gradient: 'linear-gradient(135deg, #f5a623, #e8a317)', link: '/lowcode/entity' },
  { key: 'pages', icon: LayoutIcon, value: 0, label: '页面数量', trend: '--', trendDir: 'up', trendIcon: TrendingUpIcon, gradient: 'linear-gradient(135deg, #6366f1, #8b5cf6)', link: '/lowcode/page/list' },
  { key: 'published', icon: RocketIcon, value: 0, label: '已发布数', trend: '--', trendDir: 'up', trendIcon: TrendingUpIcon, gradient: 'linear-gradient(135deg, #10b981, #34d399)', link: null },
  { key: 'drafts', icon: FileIcon, value: 0, label: '草稿数', trend: '--', trendDir: 'down', trendIcon: TrendingDownIcon, gradient: 'linear-gradient(135deg, #06b6d4, #22d3ee)', link: null },
]);

const topEntities = ref<any[]>([]);
const businessApps = ref<BusinessApp[]>([]);

// 快捷导航
const quickNavs = [
  { icon: DataBaseIcon, label: '创建实体', desc: '定义数据模型', bgColor: 'linear-gradient(135deg, #f5a623, #e8a317)', action: () => router.push('/lowcode/entity') },
  { icon: LayoutIcon, label: '设计页面', desc: '可视化拖拽', bgColor: 'linear-gradient(135deg, #6366f1, #8b5cf6)', action: () => router.push('/lowcode/page/list') },
  { icon: SettingIcon, label: '系统管理', desc: '配置与设置', bgColor: 'linear-gradient(135deg, #06b6d4, #22d3ee)', action: () => router.push('/lowcode/settings') },
  { icon: LightbulbIcon, label: '使用帮助', desc: '操作指南', bgColor: 'linear-gradient(135deg, #10b981, #34d399)', action: () => showHelpDialog.value = true },
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

function goToApp(app: BusinessApp) {
  router.push(`/run/${app.code}`);
}

function getAppIcon(iconName?: string) {
  const icons: Record<string, any> = {
    calendar: CalendarIcon,
    app: AppIcon,
    database: DataBaseIcon,
    layout: LayoutIcon,
    settings: SettingIcon,
    rocket: RocketIcon,
  };
  return icons[iconName || ''] || AppIcon;
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

    // 加载业务应用
    try {
      const appRes = await businessAppApi.getAll();
      if (appRes.data.code === 1) {
        const apps = appRes.data.data;
        businessApps.value = Array.isArray(apps) ? apps : [];
      }
    } catch {
      businessApps.value = [];
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

/* ===== 业务应用 ===== */
.app-section {
  margin-bottom: 28px;
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}
.app-header {
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
.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 14px;
}
.app-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s;
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
    border-color: #e8a317;
  }
}
.app-card-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.app-card-info {
  flex: 1;
  min-width: 0;
  h4 {
    font-size: 15px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0 0 4px;
  }
  p {
    font-size: 12px;
    color: #999;
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
.app-card-arrow {
  color: #ccc;
  flex-shrink: 0;
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

/* ===== 使用帮助弹窗 ===== */
.help-dialog :deep(.t-dialog__body) {
  padding: 0 28px 28px;
}

.help-dialog-content {
  .help-intro {
    text-align: center;
    padding: 8px 0 24px;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 24px;

    h3 {
      font-size: 18px;
      font-weight: 700;
      color: #1a1a2e;
      margin: 0 0 8px;
    }

    p {
      font-size: 13px;
      color: #6b7280;
      margin: 0;
      line-height: 1.6;
    }
  }

  .help-card-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .help-card {
    background: #fff;
    border-radius: 12px;
    border: 1px solid #e5e7eb;
    overflow: hidden;
    transition: box-shadow 0.2s, border-color 0.2s;

    &:hover {
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
      border-color: var(--card-color, #e5e7eb);
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 14px 18px;
      background: linear-gradient(135deg, color-mix(in srgb, var(--card-color, #6366f1) 8%, transparent), color-mix(in srgb, var(--card-color, #6366f1) 3%, transparent));
      border-bottom: 1px solid color-mix(in srgb, var(--card-color, #6366f1) 12%, transparent);

      .card-icon {
        width: 36px;
        height: 36px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--card-color, #6366f1);
        color: #fff;
      }

      span {
        font-size: 15px;
        font-weight: 600;
        color: #1a1a2e;
      }
    }

    .card-body {
      padding: 16px 18px;
    }
  }

  .step-item {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;

    &:last-child { margin-bottom: 0; }

    .step-num {
      width: 22px;
      height: 22px;
      border-radius: 50%;
      background: var(--card-color, #6366f1);
      color: #fff;
      font-size: 11px;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      margin-top: 2px;
    }

    .step-text {
      flex: 1;
      min-width: 0;

      b {
        font-size: 13px;
        color: #1a1a2e;
        display: block;
        margin-bottom: 2px;
      }

      p {
        font-size: 12px;
        color: #6b7280;
        margin: 0;
        line-height: 1.5;
      }
    }
  }

  .feature-list {
    list-style: none;
    margin: 0;
    padding: 0;

    li {
      font-size: 12px;
      color: #4b5563;
      line-height: 1.7;
      padding: 5px 0;
      border-bottom: 1px dashed #f0f0f0;

      &:last-child { border-bottom: none; }

      b { color: #1a1a2e; }
    }
  }

  .tip-row {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 7px 0;
    border-bottom: 1px dashed #f0f0f0;

    &:last-child { border-bottom: none; }

    kbd {
      display: inline-block;
      padding: 3px 8px;
      font-size: 11px;
      font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
      background: #f3f4f6;
      border: 1px solid #d1d5db;
      border-radius: 5px;
      color: #374151;
      white-space: nowrap;
      flex-shrink: 0;
      min-width: 60px;
      text-align: center;
    }

    span {
      font-size: 12px;
      color: #6b7280;
      line-height: 1.5;
    }
  }

  .help-footer {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-top: 24px;
    padding: 14px 20px;
    background: linear-gradient(135deg, #fffbeb, #fef3c7);
    border-radius: 10px;
    font-size: 13px;
    color: #92400e;

    b { color: #78350f; }
  }
}
</style>
