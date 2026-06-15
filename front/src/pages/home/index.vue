<template>
  <div class="home-container">
    <!-- 欢迎区域 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <div class="welcome-title">
          <RocketIcon class="title-icon" :size="'32'" />
          欢迎使用低代码开发平台
        </div>
        <p class="welcome-desc">快速构建企业级应用，让开发更简单高效</p>
        <div class="welcome-actions">
          <t-button theme="primary" size="large" @click="goTo('/entity/list')">
            <template #icon><AddCircleIcon :size="'18'" /></template>
            开始创建
          </t-button>
          <t-button size="large" @click="goTo('/page/list')">
            <template #icon><LayoutIcon :size="'18'" /></template>
            页面设计
          </t-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="(stat, index) in stats" :key="index">
        <div class="stat-icon" :style="{ background: stat.bgColor }">
          <component :is="stat.icon" :size="'24'" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
        <div class="stat-trend" :class="stat.trendClass">
          <component :is="stat.trendIcon" :size="'14'" />
          <span>{{ stat.trend }}</span>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3 class="section-title">
        <FlashlightIcon :size="'18'" />
        快捷操作
      </h3>
      <div class="action-grid">
        <div class="action-item" v-for="(action, index) in quickActions" :key="index" @click="handleAction(action.path)">
          <div class="action-icon">
            <component :is="action.icon" :size="'24'" />
          </div>
          <span class="action-label">{{ action.label }}</span>
        </div>
      </div>
    </div>

    <!-- 最近活动 -->
    <div class="recent-section">
      <div class="section-header">
        <h3 class="section-title">
          <TimeFilledIcon :size="'18'" />
          最近活动
        </h3>
        <t-button variant="text" size="small">查看全部</t-button>
      </div>
      <div class="activity-list">
        <div class="activity-item" v-for="(item, index) in recentActivities" :key="index">
          <div class="activity-dot" :style="{ background: item.color }"></div>
          <div class="activity-content">
            <span class="activity-text">{{ item.text }}</span>
            <span class="activity-time">{{ item.time }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 平台特性 -->
    <div class="features-section">
      <h3 class="section-title">
        <StarIcon :size="'18'" />
        平台特性
      </h3>
      <div class="features-grid">
        <div class="feature-card" v-for="(feature, index) in features" :key="index">
          <div class="feature-icon" :style="{ background: feature.bgColor }">
            <component :is="feature.icon" :size="'32'" />
          </div>
          <h4 class="feature-title">{{ feature.title }}</h4>
          <p class="feature-desc">{{ feature.desc }}</p>
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
  StarIcon, LayoutIcon, DataBaseIcon, TableIcon, FileIcon,
  TrendingUpIcon, TrendingDownIcon, FlashlightIcon, AddCircleIcon,
  LightbulbIcon, BookIcon, TimeFilledIcon, AppIcon,
  CodeIcon, ShieldErrorIcon, RocketIcon
} from 'tdesign-icons-vue-next';
import entityMetaApi from '../../api/lowcode/entityMeta';

const router = useRouter();

const stats = ref([
  {
    icon: DataBaseIcon,
    value: 0,
    label: '已创建实体',
    trend: '+12%',
    trendIcon: TrendingUpIcon,
    trendClass: 'positive',
    bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    icon: TableIcon,
    value: 0,
    label: '数据表',
    trend: '+8%',
    trendIcon: TrendingUpIcon,
    trendClass: 'positive',
    bgColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    icon: FileIcon,
    value: 0,
    label: '数据记录',
    trend: '+15%',
    trendIcon: TrendingUpIcon,
    trendClass: 'positive',
    bgColor: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    icon: LayoutIcon,
    value: 0,
    label: '页面模板',
    trend: '-2%',
    trendIcon: TrendingDownIcon,
    trendClass: 'negative',
    bgColor: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  }
]);

const quickActions = [
  { icon: AddCircleIcon, label: '创建实体', path: '/entity/create' },
  { icon: LayoutIcon, label: '设计页面', path: '/page/list' },
  { icon: LightbulbIcon, label: '使用指南', path: '' },
  { icon: BookIcon, label: '帮助文档', path: '' }
];

const recentActivities = ref([
  { text: '创建了实体"用户信息"', time: '5分钟前', color: '#667eea' },
  { text: '发布了实体"产品管理"', time: '35分钟前', color: '#52c41a' },
  { text: '编辑了页面"首页"', time: '1小时前', color: '#1890ff' },
  { text: '删除了草稿实体', time: '2小时前', color: '#ff4d4f' },
  { text: '导出了数据报表', time: '2小时前', color: '#722ed1' }
]);

const features = [
  {
    icon: AppIcon,
    title: '可视化设计',
    desc: '拖拽式组件设计，所见即所得的开发体验',
    bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    icon: DataBaseIcon,
    title: '数据模型',
    desc: '灵活定义数据实体，自动生成数据表',
    bgColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    icon: CodeIcon,
    title: '代码生成',
    desc: '一键生成前后端代码，快速交付',
    bgColor: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    icon: ShieldErrorIcon,
    title: '安全可靠',
    desc: '内置权限管理，数据安全有保障',
    bgColor: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  }
];

function goTo(path: string) {
  router.push(path);
}

function handleAction(path: string) {
  if (path) {
    router.push(path);
  } else {
    MessagePlugin.info('该功能开发中...');
  }
}

async function loadStats() {
  try {
    const res = await entityMetaApi.list({ page: 1, pageSize: 1 });
    if (res.data.code === 1) {
      const data = res.data.data;
      stats.value[0].value = data.totalElements || data.total || 0;
      stats.value[1].value = Math.floor(stats.value[0].value * 0.7);
      stats.value[2].value = stats.value[0].value * 120;
      stats.value[3].value = Math.floor(stats.value[0].value * 0.3);
    }
  } catch (e) {
    // 忽略错误，使用默认值
  }
}

onMounted(() => {
  loadStats();
});
</script>

<style scoped lang="less">
.home-container {
  padding: 24px;
  background: #f5f6f8;
  min-height: calc(100vh - 64px);
  border: none !important;

  * {
    border: none !important;
    box-shadow: none !important;
    border-color: transparent !important;
  }
}

.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 56px;
  margin-bottom: 24px;
  text-align: center;

  .welcome-content {
    .welcome-title {
      font-size: 32px;
      font-weight: 600;
      color: #fff;
      margin: 0 0 16px 0;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;

      .title-icon {
        animation: pulse 2s ease-in-out infinite;
      }
    }

    .welcome-desc {
      font-size: 16px;
      color: rgba(255, 255, 255, 0.85);
      margin: 0 0 32px 0;
    }
  }

  .welcome-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;

  .stat-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;

    .stat-icon {
      width: 50px;
      height: 50px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
    }

    .stat-info {
      flex: 1;

      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: #1f2329;
        margin-bottom: 4px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }

      .stat-label {
        font-size: 14px;
        color: #8f959e;
      }
    }

    .stat-trend {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 12px;

      &.positive {
        color: #52c41a;
        background: #f6ffed;
      }

      &.negative {
        color: #ff4d4f;
        background: #fff2f0;
      }
    }
  }
}

.quick-actions {
  margin-bottom: 24px;
  border: none;
  box-shadow: none;

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #1f2329;
    margin: 0 0 16px 0;
  }

  .action-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;

    .action-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px 20px;
      background: #fff;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.3s ease;
      border: none;

      &:hover {
        background: #f5f7fa;
        transform: translateY(-3px);
        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.15);
      }

      .action-icon {
        width: 44px;
        height: 44px;
        border-radius: 12px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        transition: transform 0.3s;
      }

      &:hover .action-icon {
        transform: rotate(5deg) scale(1.05);
      }

      .action-label {
        font-size: 14px;
        color: #1f2329;
        font-weight: 500;
      }
    }
  }
}

.recent-section {
  margin-bottom: 24px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #1f2329;
    margin: 0;
  }

  .activity-list {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    border: none;
    box-shadow: none;

    .activity-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 0;

      &:not(:last-child) {
        border-bottom: 1px solid #f0f0f0;
      }

      .activity-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        flex-shrink: 0;
      }

      .activity-content {
        flex: 1;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .activity-text {
          font-size: 14px;
          color: #1f2329;
        }

        .activity-time {
          font-size: 12px;
          color: #bbbfc4;
        }
      }
    }
  }
}

.features-section {
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #1f2329;
    margin: 0 0 16px 0;
  }

  .features-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;

    .feature-card {
      background: #fff;
      border-radius: 12px;
      padding: 24px;
      text-align: center;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
      }

      .feature-icon {
        width: 64px;
        height: 64px;
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        margin: 0 auto 16px;
      }

      .feature-title {
        font-size: 16px;
        font-weight: 600;
        color: #1f2329;
        margin: 0 0 8px 0;
      }

      .feature-desc {
        font-size: 14px;
        color: #8f959e;
        margin: 0;
        line-height: 1.5;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}
</style>
