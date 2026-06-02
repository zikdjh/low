<template>
  <div class="home-page">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">
          <t-icon name="sparkles" class="title-icon" />
          欢迎使用低代码开发平台
        </h1>
        <p class="welcome-subtitle">快速构建企业级应用，无需编写大量代码</p>
      </div>
      <div class="welcome-actions">
        <t-button theme="primary" size="large" @click="goTo('/lowcode/entity')">
          <template #icon><t-icon name="rocket" /></template>
          开始创建
        </t-button>
        <t-button size="large" @click="goTo('/lowcode/page')">
          <template #icon><t-icon name="layout" /></template>
          页面设计
        </t-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <t-card class="stat-card">
        <div class="stat-header">
          <div class="stat-icon blue">
            <t-icon name="database" size="28" />
          </div>
          <div class="stat-trend positive">
            <t-icon name="trending-up" size="16" />
            12%
          </div>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.entities }}</div>
          <div class="stat-label">已创建实体</div>
        </div>
        <div class="stat-progress">
          <t-progress :percent="45" color="#1677ff" :show-text="false" />
        </div>
      </t-card>

      <t-card class="stat-card">
        <div class="stat-header">
          <div class="stat-icon green">
            <t-icon name="table" size="28" />
          </div>
          <div class="stat-trend positive">
            <t-icon name="trending-up" size="16" />
            8%
          </div>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.tables }}</div>
          <div class="stat-label">数据表</div>
        </div>
        <div class="stat-progress">
          <t-progress :percent="62" color="#52c41a" :show-text="false" />
        </div>
      </t-card>

      <t-card class="stat-card">
        <div class="stat-header">
          <div class="stat-icon orange">
            <t-icon name="file-text" size="28" />
          </div>
          <div class="stat-trend positive">
            <t-icon name="trending-up" size="16" />
            15%
          </div>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.records }}</div>
          <div class="stat-label">数据记录</div>
        </div>
        <div class="stat-progress">
          <t-progress :percent="38" color="#fa8c16" :show-text="false" />
        </div>
      </t-card>

      <t-card class="stat-card">
        <div class="stat-header">
          <div class="stat-icon purple">
            <t-icon name="layout" size="28" />
          </div>
          <div class="stat-trend negative">
            <t-icon name="trending-down" size="16" />
            2%
          </div>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pages }}</div>
          <div class="stat-label">页面模板</div>
        </div>
        <div class="stat-progress">
          <t-progress :percent="25" color="#722ed1" :show-text="false" />
        </div>
      </t-card>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3 class="section-title">
        <t-icon name="zap" size="18" />
        快捷操作
      </h3>
      <div class="action-grid">
        <div class="action-item" @click="goTo('/lowcode/entity')">
          <div class="action-icon">
            <t-icon name="plus-circle" size="24" />
          </div>
          <span class="action-label">创建实体</span>
        </div>
        <div class="action-item" @click="goTo('/lowcode/page')">
          <div class="action-icon">
            <t-icon name="layout" size="24" />
          </div>
          <span class="action-label">设计页面</span>
        </div>
        <div class="action-item" @click="showTips">
          <div class="action-icon">
            <t-icon name="lightbulb" size="24" />
          </div>
          <span class="action-label">使用指南</span>
        </div>
        <div class="action-item" @click="showDocs">
          <div class="action-icon">
            <t-icon name="book" size="24" />
          </div>
          <span class="action-label">帮助文档</span>
        </div>
      </div>
    </div>

    <!-- 最近活动 -->
    <div class="recent-section">
      <div class="section-header">
        <h3 class="section-title">
          <t-icon name="history" size="18" />
          最近活动
        </h3>
        <t-button variant="text" size="small">查看全部</t-button>
      </div>
      <t-card class="activity-card">
        <t-timeline>
          <t-timeline-item v-for="(item, index) in recentActivities" :key="index">
            <template #icon>
              <t-icon :name="item.icon" :size="16" />
            </template>
            <template #content>
              <div class="activity-content">
                <span class="activity-text">{{ item.text }}</span>
                <span class="activity-time">{{ item.time }}</span>
              </div>
            </template>
          </t-timeline-item>
        </t-timeline>
      </t-card>
    </div>

    <!-- 功能介绍 -->
    <div class="features-section">
      <h3 class="section-title">
        <t-icon name="star" size="18" />
        平台特性
      </h3>
      <div class="features-grid">
        <t-card class="feature-card" hover-shadow>
          <div class="feature-icon">
            <t-icon name="blocks" size="32" />
          </div>
          <h4 class="feature-title">可视化设计</h4>
          <p class="feature-desc">拖拽式组件设计，所见即所得的开发体验</p>
        </t-card>
        <t-card class="feature-card" hover-shadow>
          <div class="feature-icon">
            <t-icon name="database" size="32" />
          </div>
          <h4 class="feature-title">数据模型</h4>
          <p class="feature-desc">灵活定义数据实体，自动生成数据表</p>
        </t-card>
        <t-card class="feature-card" hover-shadow>
          <div class="feature-icon">
            <t-icon name="code" size="32" />
          </div>
          <h4 class="feature-title">代码生成</h4>
          <p class="feature-desc">一键生成前后端代码，快速交付</p>
        </t-card>
        <t-card class="feature-card" hover-shadow>
          <div class="feature-icon">
            <t-icon name="shield" size="32" />
          </div>
          <h4 class="feature-title">安全可靠</h4>
          <p class="feature-desc">内置权限管理，数据安全有保障</p>
        </t-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import entityMetaApi from '../../api/lowcode/entityMeta';

const router = useRouter();

const stats = ref({
  entities: 0,
  tables: 0,
  records: 0,
  pages: 0,
});

const recentActivities = ref([
  { icon: 'plus-circle', text: '创建了实体 "用户信息"', time: '5分钟前' },
  { icon: 'check-circle', text: '发布了实体 "产品管理"', time: '15分钟前' },
  { icon: 'edit', text: '编辑了页面 "首页"', time: '30分钟前' },
  { icon: 'delete', text: '删除了草稿实体', time: '1小时前' },
  { icon: 'download', text: '导出了数据报表', time: '2小时前' },
]);

function goTo(path: string) {
  router.push(path);
}

function showTips() {
  MessagePlugin.info('使用指南开发中...');
}

function showDocs() {
  MessagePlugin.info('帮助文档开发中...');
}

async function loadStats() {
  try {
    const res = await entityMetaApi.list({ page: 1, pageSize: 1 });
    if (res.data.code === 1) {
      const data = res.data.data;
      stats.value.entities = data.totalElements || data.total || 0;
      stats.value.tables = Math.floor(stats.value.entities * 0.7);
      stats.value.records = stats.value.entities * 120;
      stats.value.pages = Math.floor(stats.value.entities * 0.3);
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
.home-page {
  padding: 24px;
  background: #f5f6f8;
  min-height: calc(100vh - 64px);
}

.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 48px;
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
    
    .welcome-subtitle {
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

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  
  .stat-card {
    padding: 20px;
    border-radius: 12px;
    background: #fff;
    
    .stat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
    }
    
    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      
      &.blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
      &.green { background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%); }
      &.orange { background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%); }
      &.purple { background: linear-gradient(135deg, #722ed1 0%, #531dab 100%); }
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
    
    .stat-info {
      margin-bottom: 12px;
      
      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: #1f2329;
        margin-bottom: 4px;
      }
      
      .stat-label {
        font-size: 14px;
        color: #8f959e;
      }
    }
    
    .stat-progress {
      height: 6px;
    }
  }
}

.quick-actions {
  margin-bottom: 24px;
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 500;
    color: #1f2329;
    margin-bottom: 16px;
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
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
      
      &:hover {
        background: #f5f6f8;
        transform: translateY(-2px);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      }
      
      .action-icon {
        width: 40px;
        height: 40px;
        border-radius: 10px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
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
    font-weight: 500;
    color: #1f2329;
  }
  
  .activity-card {
    background: #fff;
    border-radius: 8px;
    
    .activity-content {
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

.features-section {
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 500;
    color: #1f2329;
    margin-bottom: 16px;
  }
  
  .features-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    
    .feature-card {
      padding: 24px;
      text-align: center;
      border-radius: 12px;
      
      .feature-icon {
        width: 56px;
        height: 56px;
        border-radius: 14px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        margin: 0 auto 16px auto;
      }
      
      .feature-title {
        font-size: 16px;
        font-weight: 500;
        color: #1f2329;
        margin: 0 0 8px 0;
      }
      
      .feature-desc {
        font-size: 13px;
        color: #8f959e;
        margin: 0;
        line-height: 1.6;
      }
    }
  }
}
</style>
