<template>
  <div class="admin-page">
    <div class="admin-page-header">
      <h2>实体管理</h2>
      <p>查看所有用户创建的实体模型</p>
    </div>

    <div class="admin-page-body">
      <t-table
        :data="entities"
        :columns="columns"
        :loading="loading"
        row-key="id"
        stripe
        hover
        bordered
        size="medium"
      >
        <template #status="{ row }">
          <t-tag
            :theme="row.status === 'published' ? 'success' : row.status === 'archived' ? 'default' : 'warning'"
            variant="light"
          >
            {{ row.status === 'published' ? '已发布' : row.status === 'archived' ? '已归档' : '草稿' }}
          </t-tag>
        </template>
        <template #createdAt="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </t-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { MessagePlugin } from 'tdesign-vue-next';
import adminApi from '../../api/admin';

interface AdminEntity {
  id: number;
  code: string;
  name: string;
  tableName: string;
  status: string;
  description?: string;
  createdAt: string;
}

const entities = ref<AdminEntity[]>([]);
const loading = ref(false);

const columns = [
  { colKey: 'id', title: 'ID', width: 70 },
  { colKey: 'name', title: '实体名称', width: 160 },
  { colKey: 'code', title: '实体编码', width: 160 },
  { colKey: 'tableName', title: '数据库表名', width: 170 },
  { colKey: 'status', title: '状态', width: 90 },
  { colKey: 'description', title: '描述', ellipsis: true },
  { colKey: 'createdAt', title: '创建时间', width: 170 },
];

function formatDate(dateStr?: string) {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString('zh-CN');
}

async function loadEntities() {
  loading.value = true;
  try {
    const resp: any = await adminApi.listEntities();
    if (resp?.code === 1) {
      entities.value = resp.data as AdminEntity[];
    } else {
      MessagePlugin.error(resp?.msg || '加载实体列表失败');
    }
  } catch (e: any) {
    const detail = e?.response?.data?.msg || e?.message || '未知错误';
    console.error('加载实体列表异常', detail, e);
    if (e?.response?.status === 401 || e?.response?.status === 403) {
      MessagePlugin.error('权限不足，请重新登录');
    } else {
      MessagePlugin.error('加载实体列表失败: ' + detail);
    }
  } finally {
    loading.value = false;
  }
}

onMounted(() => { loadEntities(); });
</script>

<style scoped lang="less">
.admin-page {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.admin-page-header {
  margin-bottom: 24px;
  h2 { font-size: 18px; font-weight: 600; color: #1a1a1a; margin: 0 0 4px; }
  p { font-size: 13px; color: #999; margin: 0; }
}
</style>
