<template>
  <div class="entity-view-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <BackButton to="/lowcode/entity" label="返回实体列表" />
        <h2>{{ entityData?.name || '实体详情' }}</h2>
        <t-tag 
          :theme="getStatusTheme(entityData?.status)" 
          variant="light"
          size="small"
        >
          {{ getStatusText(entityData?.status) }}
        </t-tag>
      </div>
      <t-space>
        <t-button 
          variant="outline" 
          @click="goEdit"
        >
          <template #icon><EditIcon /></template>
          编辑
        </t-button>
        <t-button 
          v-if="entityData?.status === 'published'"
          theme="primary" 
          @click="goDataManage"
        >
          <template #icon><DataBaseIcon /></template>
          数据管理
        </t-button>
      </t-space>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <t-loading size="large" text="加载中..." />
    </div>

    <!-- 实体详情 -->
    <template v-else>
      <!-- 基本信息 -->
      <t-card title="基本信息" class="section-card">
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">实体编码</span>
            <code class="info-value code">{{ entityData?.code }}</code>
          </div>
          <div class="info-item">
            <span class="info-label">实体名称</span>
            <span class="info-value">{{ entityData?.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">数据库表名</span>
            <code class="info-value code">{{ entityData?.tableName }}</code>
          </div>
          <div class="info-item">
            <span class="info-label">字段数量</span>
            <span class="info-value">{{ entityData?.fieldCount || 0 }} 个</span>
          </div>
          <div class="info-item full-width">
            <span class="info-label">描述</span>
            <p class="info-value description">{{ entityData?.description || '-' }}</p>
          </div>
        </div>
      </t-card>

      <!-- 字段列表 -->
      <t-card 
        title="字段列表" 
        class="section-card"
        v-if="entityData?.fields?.length > 0"
      >
        <t-table
          :data="entityData.fields"
          :columns="fieldColumns"
          row-key="code"
          stripe
          show-header-overflow
          show-row-overflow
        >
          <template #fieldType="{ row }">
            <t-tag variant="light" theme="primary" size="small">
              {{ row.fieldType }}
            </t-tag>
          </template>
          <template #length="{ row }">
            <span v-if="row.length">{{ row.length }}</span>
            <span v-else style="color: #ccc;">-</span>
          </template>
          <template #nullable="{ row }">
            <CheckCircleIcon v-if="row.nullable" style="color: #22c55e;" />
            <CircleIcon v-else style="color: #f59e0b;" />
          </template>
          <template #showInList="{ row }">
            <CheckCircleIcon v-if="row.showInList" style="color: #22c55e;" />
            <CircleIcon v-else style="color: #9ca3af;" />
          </template>
          <template #showInForm="{ row }">
            <CheckCircleIcon v-if="row.showInForm" style="color: #22c55e;" />
            <CircleIcon v-else style="color: #9ca3af;" />
          </template>
          <template #showInSearch="{ row }">
            <CheckCircleIcon v-if="row.showInSearch" style="color: #22c55e;" />
            <CircleIcon v-else style="color: #9ca3af;" />
          </template>
          <template #reference="{ row }">
            <span v-if="row.referenceEntityCode" class="reference-info">
              {{ row.referenceEntityCode }}
              <span v-if="row.referenceDisplayFieldCode" class="reference-field">
                .{{ row.referenceDisplayFieldCode }}
              </span>
            </span>
            <span v-else>-</span>
          </template>
        </t-table>
        <div v-if="entityData.fields.length === 0" class="empty-hint">
          <p>该实体暂无字段</p>
        </div>
      </t-card>

      <!-- 空状态 -->
      <div v-if="!entityData" class="empty-container">
        <DataBaseIcon size="64" style="color: #d1d5db;" />
        <h3>实体不存在</h3>
        <p>未找到该实体，请检查实体ID是否正确</p>
        <t-button theme="primary" @click="goBack">返回</t-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import BackButton from '../../../components/common/BackButton.vue';
import {
  EditIcon, DataBaseIcon, CheckCircleIcon, CircleIcon
} from 'tdesign-icons-vue-next';
import entityMetaApi from '../../../api/lowcode/entityMeta';

const router = useRouter();
const route = useRoute();

const loading = ref(true);
const entityData = ref<any>(null);

const fieldColumns = [
  { colKey: 'code', title: '字段编码', width: 160, ellipsis: true },
  { colKey: 'name', title: '字段名称', width: 140, ellipsis: true },
  { colKey: 'fieldType', title: '字段类型', width: 140 },
  { colKey: 'reference', title: '关联实体', width: 160, ellipsis: true },
  { colKey: 'length', title: '长度', width: 80 },
  { colKey: 'nullable', title: '可为空', width: 80 },
  { colKey: 'showInList', title: '列表显示', width: 90 },
  { colKey: 'showInForm', title: '表单显示', width: 90 },
  { colKey: 'showInSearch', title: '可搜索', width: 80 },
];

function getStatusText(status?: string) {
  const map: Record<string, string> = {
    draft: '草稿',
    published: '已发布',
    archived: '已归档',
  };
  return map[status || ''] || status || '-';
}

function getStatusTheme(status?: string) {
  const map: Record<string, string> = {
    draft: 'default',
    published: 'success',
    archived: 'warning',
  };
  return map[status || ''] || 'default';
}

async function loadEntity() {
  loading.value = true;
  const id = route.params.id as string;
  if (!id) {
    loading.value = false;
    return;
  }
  try {
    const res = await entityMetaApi.getById(parseInt(id));
    if (res.data.code === 1) {
      entityData.value = res.data.data;
    }
  } catch (e: any) {
    console.error('加载实体失败:', e);
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.push('/lowcode/entity');
}

function goEdit() {
  const id = route.params.id as string;
  router.push(`/lowcode/entity/${id}`);
}

function goDataManage() {
  router.push(`/lowcode/data/${entityData.value.code}`);
}

onMounted(() => {
  loadEntity();
});
</script>

<style scoped lang="less">
.entity-view-page {
  padding: 24px;
  background: #f7f8fa;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  
  .header-left {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    h2 {
      font-size: 20px;
      font-weight: 600;
      color: #1a1a1a;
      margin: 0;
    }
  }
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.section-card {
  margin-bottom: 16px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px 24px;
  
  .info-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
    
    &.full-width {
      grid-column: 1 / -1;
    }
  }
  
  .info-label {
    font-size: 13px;
    color: #999;
    font-weight: 500;
  }
  
  .info-value {
    font-size: 14px;
    color: #1a1a1a;
    font-weight: 500;
    
    &.code {
      font-family: 'JetBrains Mono', 'Consolas', monospace;
      color: var(--td-brand-color, #E8A317);
      background: var(--td-brand-color-1, #fffbeb);
      padding: 4px 10px;
      border-radius: 6px;
      display: inline-block;
      width: fit-content;
    }
    
    &.description {
      margin: 0;
      line-height: 1.6;
      color: #666;
      font-weight: normal;
    }
  }
}

.empty-hint {
  text-align: center;
  padding: 40px;
  color: #999;
  
  p {
    margin: 0;
  }
}

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #999;
  gap: 12px;
  
  h3 {
    font-size: 18px;
    color: #666;
    margin: 0;
  }
  
  p {
    font-size: 14px;
    margin: 0;
  }
}

.reference-info {
  color: #3b82f6;
  font-family: 'JetBrains Mono', 'Consolas', monospace;
  font-size: 13px;
  
  .reference-field {
    color: #64748b;
  }
}

:deep(.t-table) {
  .t-icon {
    font-size: 16px;
  }
}
</style>