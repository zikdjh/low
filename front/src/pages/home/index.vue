<template>
  <div class="home-page">
    <div class="page-header">
      <h2>欢迎使用低代码开发平台</h2>
      <p>快速构建企业级应用</p>
    </div>

    <div class="feature-cards">
      <t-card class="feature-card" hover-shadow>
        <template #icon>
          <t-icon name="code" size="32" />
        </template>
        <template #title>实体管理</template>
        <template #description>创建和管理业务实体，定义数据模型</template>
        <t-button theme="primary" @click="goTo('/lowcode/entity')">
          立即体验
        </t-button>
      </t-card>

      <t-card class="feature-card" hover-shadow>
        <template #icon>
          <t-icon name="database" size="32" />
        </template>
        <template #title>数据管理</template>
        <template #description>可视化管理实体数据，支持增删改查</template>
        <t-button theme="primary" @click="goTo('/lowcode/entity')">
          了解更多
        </t-button>
      </t-card>

      <t-card class="feature-card" hover-shadow>
        <template #icon>
          <t-icon name="form" size="32" />
        </template>
        <template #title>表单生成</template>
        <template #description>根据实体定义自动生成表单</template>
        <t-button theme="primary" @click="goTo('/lowcode/entity')">
          了解更多
        </t-button>
      </t-card>

      <t-card class="feature-card" hover-shadow>
        <template #icon>
          <t-icon name="layout" size="32" />
        </template>
        <template #title>页面设计</template>
        <template #description>拖拽式页面设计器，所见即所得</template>
        <t-button theme="primary" @click="goTo('/lowcode/entity')">
          了解更多
        </t-button>
      </t-card>
    </div>

    <div class="stats-section">
      <t-statistic title="已创建实体" :value="entityCount" suffix="个" />
      <t-statistic title="数据表" :value="tableCount" suffix="张" />
      <t-statistic title="数据记录" :value="recordCount" suffix="条" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const entityCount = ref(0);
const tableCount = ref(0);
const recordCount = ref(0);

function goTo(path: string) {
  router.push(path);
}
</script>

<style scoped lang="less">
.home-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 32px;
}

.page-header h2 {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.page-header p {
  color: #8c8c8c;
  font-size: 16px;
  margin: 0;
}

.feature-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.feature-card {
  text-align: center;
}

.stats-section {
  display: flex;
  gap: 48px;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
}
</style>
