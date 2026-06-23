<template>
  <div class="notification-page">
    <div class="page-header">
      <div class="header-left">
        <BackButton to="/home" label="返回首页" class="notif-back-btn" />
        <div class="header-title">
          <h2>通知中心</h2>
          <p>查看所有系统通知和消息</p>
        </div>
      </div>
      <div class="header-actions">
        <t-button variant="outline" @click="notifStore.markAllAsRead()" :disabled="notifStore.unreadCount === 0">
          全部已读
        </t-button>
        <t-button variant="outline" theme="danger" @click="handleClearAll" :disabled="notifStore.notifications.length === 0">
          清空全部
        </t-button>
      </div>
    </div>

    <!-- 分类筛选 -->
    <div class="filter-bar">
      <t-space :size="4">
        <t-tag
          v-for="cat in categories"
          :key="cat.key"
          :theme="activeCategory === cat.key ? 'primary' : 'default'"
          :variant="activeCategory === cat.key ? 'solid' : 'light'"
          size="medium"
          @click="activeCategory = cat.key"
          style="cursor: pointer;"
        >
          {{ cat.label }}
          <t-badge :value="getCategoryCount(cat.key)" :max="99" />
        </t-tag>
      </t-space>
    </div>

    <!-- 通知表格 -->
    <t-card class="notification-card">
      <t-table
        :data="filteredNotifications"
        :columns="columns"
        :loading="loading"
        row-key="id"
        stripe
        hover
        :pagination="pagination"
        @page-change="onPageChange"
      >
        <template #type="{ row }">
          <t-tag :theme="getTypeTheme(row.type)" variant="light" size="small">
            {{ getTypeLabel(row.type) }}
          </t-tag>
        </template>
        <template #status="{ row }">
          <t-tag :theme="row.read ? 'default' : 'primary'" variant="light" size="small">
            {{ row.read ? '已读' : '未读' }}
          </t-tag>
        </template>
        <template #action="{ row }">
          <t-space :size="4">
            <t-button
              size="small"
              variant="text"
              @click="notifStore.markAsRead(row.id)"
              v-if="!row.read"
            >
              <template #icon><t-icon name="check" /></template>
              标为已读
            </t-button>
            <t-popconfirm content="确认删除此通知？" @confirm="notifStore.removeNotification(row.id)">
              <t-button size="small" variant="text" theme="danger">
                <template #icon><t-icon name="delete" /></template>
                删除
              </t-button>
            </t-popconfirm>
          </t-space>
        </template>
        <template #title="{ row }">
          <div :class="{ 'unread-title': !row.read }">
            {{ row.title }}
          </div>
        </template>
      </t-table>

      <!-- 空状态 -->
      <div v-if="!loading && filteredNotifications.length === 0" class="empty-state">
        <NotificationIcon size="48" />
        <h3>暂无通知</h3>
        <p>{{ activeCategory === 'all' ? '所有通知已处理完毕' : '该分类下没有通知' }}</p>
      </div>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { MessagePlugin } from 'tdesign-vue-next';
import { NotificationIcon } from 'tdesign-icons-vue-next';
import BackButton from '../../components/common/BackButton.vue';
import { useNotificationStore } from '../../store';
import { notificationApi, type NotificationResponse } from '../../api/notification';

const notifStore = useNotificationStore();
const activeCategory = ref<string>('all');
const loading = ref(false);

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

const categories = [
  { key: 'all', label: '全部' },
  { key: 'system', label: '系统' },
  { key: 'task', label: '任务' },
  { key: 'team', label: '团队' },
  { key: 'other', label: '其他' },
];

const columns = [
  { colKey: 'title', title: '标题', ellipsis: true, width: 300 },
  { colKey: 'type', title: '类型', width: 80 },
  { colKey: 'status', title: '状态', width: 80 },
  { colKey: 'time', title: '创建时间', width: 150 },
  { colKey: 'action', title: '操作', width: 180 },
];

const filteredNotifications = computed(() => {
  let result = notifStore.notifications;
  if (activeCategory.value !== 'all') {
    result = result.filter(n => n.category === activeCategory.value);
  }
  return result;
});

function getCategoryCount(key: string) {
  if (key === 'all') return notifStore.notifications.length;
  return notifStore.notifications.filter(n => n.category === key).length;
}

function handleClearAll() {
  notifStore.clearAll();
  MessagePlugin.success('已清空所有通知');
}

function getTypeTheme(type: string) {
  switch (type) {
    case 'success': return 'success';
    case 'warning': return 'warning';
    case 'error': return 'danger';
    case 'system': return 'primary';
    default: return 'default';
  }
}

function getTypeLabel(type: string) {
  switch (type) {
    case 'success': return '成功';
    case 'warning': return '警告';
    case 'error': return '错误';
    case 'system': return '系统';
    case 'info': return '消息';
    default: return '其他';
  }
}

function onPageChange(pageInfo: { current: number; pageSize: number }) {
  pagination.value.current = pageInfo.current;
  pagination.value.pageSize = pageInfo.pageSize;
}

async function loadNotifications() {
  loading.value = true;
  try {
    const response = await notificationApi.getPublishedNotifications();
    notifStore.clearAll();
    const data = response.data || response;
    let notificationsData: any[] = [];
    
    if (Array.isArray(data)) {
      notificationsData = data;
    } else if (data.code === 1 && data.data) {
      notificationsData = data.data;
    }
    
    const notifications = notificationsData.map((item: NotificationResponse) => ({
      id: String(item.id),
      title: item.title,
      description: item.content,
      type: item.type === 'warning' ? 'warning' : item.type === 'error' ? 'error' : item.type === 'success' ? 'success' : 'info',
      category: 'system',
      time: formatTime(item.publishedAt || item.createdAt),
      read: false
    }));
    
    notifications.forEach(notif => {
      notifStore.addNotification(notif);
    });
  } catch (error) {
    console.error('加载通知失败:', error);
  } finally {
    loading.value = false;
  }
}

function formatTime(dateStr: string | null) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  return date.toLocaleDateString('zh-CN');
}

onMounted(() => {
  loadNotifications();
});
</script>

<style scoped lang="less">
.notification-page {
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
    align-items: flex-start;
    gap: 12px;
  }

  .notif-back-btn {
    margin-top: 2px;
    flex-shrink: 0;
  }
}

.header-title h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 6px;
}

.header-title p {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.filter-bar {
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.notification-card {
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.unread-title {
  font-weight: 600;
  color: #1a1a1a;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #ccc;
  h3 { font-size: 16px; color: #999; margin: 16px 0 8px; }
  p { font-size: 13px; color: #bbb; margin: 0; }
}
</style>