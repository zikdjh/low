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
      <div
        v-for="cat in categories"
        :key="cat.key"
        class="filter-tab"
        :class="{ active: activeCategory === cat.key }"
        @click="activeCategory = cat.key"
      >
        <span class="filter-dot" :style="{ background: cat.color }"></span>
        {{ cat.label }}
        <span class="filter-count">{{ getCategoryCount(cat.key) }}</span>
      </div>
    </div>

    <!-- 通知列表 -->
    <div class="notification-container" v-if="filteredNotifications.length > 0">
      <div
        v-for="item in filteredNotifications"
        :key="item.id"
        class="notif-card"
        :class="{ unread: !item.read }"
      >
        <div class="notif-icon-box" :style="{ background: getNotifBg(item.type) }">
          <component :is="getNotifIcon(item.type)" size="18" />
        </div>
        <div class="notif-body" @click="notifStore.markAsRead(item.id)">
          <div class="notif-top">
            <span class="notif-title">{{ item.title }}</span>
            <span class="notif-time">{{ item.time }}</span>
          </div>
          <p class="notif-desc">{{ item.description }}</p>
          <div class="notif-meta">
            <t-tag size="small" variant="light" :theme="getTypeTheme(item.type)">{{ getTypeLabel(item.type) }}</t-tag>
            <span class="notif-status">{{ item.read ? '已读' : '未读' }}</span>
          </div>
        </div>
        <div class="notif-dot" v-if="!item.read"></div>
        <button class="notif-delete" @click="notifStore.removeNotification(item.id)" title="删除">
          <CloseIcon size="14" />
        </button>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <NotificationIcon size="48" />
      <h3>暂无通知</h3>
      <p>{{ activeCategory === 'all' ? '所有通知已处理完毕' : '该分类下没有通知' }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { MessagePlugin } from 'tdesign-vue-next';
import {
  NotificationIcon, CloseIcon, CheckCircleIcon,
  TipsIcon, ErrorCircleFilledIcon, InfoCircleFilledIcon
} from 'tdesign-icons-vue-next';
import BackButton from '../../components/common/BackButton.vue';
import { useNotificationStore } from '../../store';
import type { AppNotification } from '../../store/modules/notification';

const notifStore = useNotificationStore();
const activeCategory = ref<string>('all');

const categories = [
  { key: 'all', label: '全部', color: '#666' },
  { key: 'system', label: '系统', color: '#f5a623' },
  { key: 'task', label: '任务', color: '#52c41a' },
  { key: 'team', label: '团队', color: '#1677ff' },
  { key: 'other', label: '其他', color: '#8c8c8c' },
];

const filteredNotifications = computed(() => {
  if (activeCategory.value === 'all') return notifStore.notifications;
  if (activeCategory.value === 'unread') return notifStore.unreadNotifications;
  return notifStore.notifications.filter(n => n.category === activeCategory.value);
});

function getCategoryCount(key: string) {
  if (key === 'all') return notifStore.notifications.length;
  return notifStore.notifications.filter(n => n.category === key).length;
}

function handleClearAll() {
  notifStore.clearAll();
  MessagePlugin.success('已清空所有通知');
}

function getNotifIcon(type: string) {
  switch (type) {
    case 'success': return CheckCircleIcon;
    case 'warning': return ErrorCircleFilledIcon;
    case 'error': return ErrorCircleFilledIcon;
    case 'system': return TipsIcon;
    default: return InfoCircleFilledIcon;
  }
}

function getNotifBg(type: string) {
  switch (type) {
    case 'success': return 'linear-gradient(135deg, #52c41a, #73d13d)';
    case 'warning': return 'linear-gradient(135deg, #fa8c16, #ffa940)';
    case 'error': return 'linear-gradient(135deg, #ff4d4f, #ff7875)';
    case 'system': return 'linear-gradient(135deg, #f5a623, #e8a317)';
    default: return 'linear-gradient(135deg, #1677ff, #69b1ff)';
  }
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
</script>

<style scoped lang="less">
.notification-page {
  padding: 28px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;

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
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 6px;
}
.header-title p {
  font-size: 14px;
  color: #999;
  margin: 0;
}
.header-actions {
  display: flex;
  gap: 8px;
}

.filter-bar {
  display: flex;
  gap: 4px;
  background: #fff;
  border-radius: 12px;
  padding: 6px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.filter-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  &:hover { background: #f5f5f5; color: #333; }
  &.active {
    background: linear-gradient(135deg, rgba(245,166,35,0.1), rgba(232,163,23,0.1));
    color: #e8a317;
    font-weight: 600;
  }
}
.filter-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.filter-count {
  font-size: 12px;
  background: #f5f5f5;
  padding: 1px 7px;
  border-radius: 10px;
}

.notification-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notif-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px 20px;
  display: flex;
  gap: 14px;
  align-items: flex-start;
  transition: all 0.2s;
  border: 1px solid transparent;
  box-shadow: 0 1px 3px rgba(0,0,0,0.03);
  position: relative;
  &:hover {
    border-color: #f0f0f0;
    box-shadow: 0 4px 16px rgba(0,0,0,0.05);
    .notif-delete { opacity: 1; }
  }
  &.unread {
    background: #fffdf5;
    border-color: rgba(245,166,35,0.15);
  }
}
.notif-icon-box {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.notif-body {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}
.notif-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.notif-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
}
.notif-time {
  font-size: 12px;
  color: #bbb;
  flex-shrink: 0;
  margin-left: 12px;
}
.notif-desc {
  font-size: 13px;
  color: #777;
  margin: 0 0 8px;
  line-height: 1.5;
}
.notif-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}
.notif-status {
  font-size: 12px;
  color: #bbb;
}
.notif-dot {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 8px;
  height: 8px;
  background: #e8a317;
  border-radius: 50%;
}
.notif-delete {
  opacity: 0;
  background: none;
  border: none;
  color: #ccc;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  transition: all 0.2s;
  &:hover { background: #fff2f0; color: #ff4d4f; }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #ccc;
  h3 { font-size: 16px; color: #999; margin: 16px 0 8px; }
  p { font-size: 13px; color: #bbb; margin: 0; }
}
</style>
