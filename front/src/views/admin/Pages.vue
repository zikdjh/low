<template>
  <div class="admin-page">
    <div class="admin-page-header">
      <h2>页面管理</h2>
      <p>查看所有用户创建的页面</p>
    </div>

    <div class="admin-page-body">
      <t-table
        :data="pages"
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
        <template #action="{ row }">
          <t-button variant="text" @click="viewPage(row)">
            <template #icon><BrowseIcon /></template>
            浏览
          </t-button>
        </template>
      </t-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import { BrowseIcon } from 'tdesign-icons-vue-next';
import adminApi from '../../api/admin';

const router = useRouter();

interface AdminPage {
  id: number;
  pageCode: string;
  name: string;
  pageType: string;
  status: string;
  description?: string;
  createdAt: string;
}

const pages = ref<AdminPage[]>([]);
const loading = ref(false);

const columns = [
  { colKey: 'id', title: 'ID', width: 70 },
  { colKey: 'name', title: '页面名称', width: 180 },
  { colKey: 'pageCode', title: '页面编码', width: 160 },
  { colKey: 'pageType', title: '页面类型', width: 100 },
  { colKey: 'status', title: '状态', width: 90 },
  { colKey: 'description', title: '描述', ellipsis: true },
  { colKey: 'createdAt', title: '创建时间', width: 170 },
  { colKey: 'action', title: '操作', width: 100 },
];

function formatDate(dateStr?: string) {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString('zh-CN');
}

function viewPage(row: AdminPage) {
  router.push(`/lowcode/page/preview/${row.pageCode}`);
}

async function loadPages() {
  loading.value = true;
  try {
    const resp: any = await adminApi.listPages();
    if (resp?.code === 1) {
      pages.value = resp.data as AdminPage[];
    } else {
      MessagePlugin.error(resp?.msg || '加载页面列表失败');
    }
  } catch (e: any) {
    const detail = e?.response?.data?.msg || e?.message || '未知错误';
    console.error('加载页面列表异常', detail, e);
    if (e?.response?.status === 401 || e?.response?.status === 403) {
      MessagePlugin.error('权限不足，请重新登录');
    } else {
      MessagePlugin.error('加载页面列表失败: ' + detail);
    }
  } finally {
    loading.value = false;
  }
}

onMounted(() => { loadPages(); });
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
