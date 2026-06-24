import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { notificationApi } from '@/api/notification';

export interface AppNotification {
  id: string;
  title: string;
  description: string;
  time: string;
  read: boolean;
  type: 'info' | 'success' | 'warning' | 'error' | 'system';
  category: 'system' | 'task' | 'team' | 'other';
}

const READ_IDS_KEY = 'app_read_notification_ids';

function getReadIds(): Set<string> {
  try {
    const raw = localStorage.getItem(READ_IDS_KEY);
    if (raw) {
      return new Set(JSON.parse(raw));
    }
  } catch {}
  return new Set();
}

function saveReadIds(ids: Set<string>) {
  localStorage.setItem(READ_IDS_KEY, JSON.stringify([...ids]));
}

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<AppNotification[]>([]);
  const readIds = ref(getReadIds());

  async function loadNotifications() {
    try {
      const response = await notificationApi.getPublishedNotifications();
      const data = response.data || response;
      const currentReadIds = readIds.value;
      const mapper = (item: any): AppNotification => ({
        id: String(item.id),
        title: item.title,
        description: item.content,
        time: formatTime(item.publishedAt || item.createdAt),
        read: currentReadIds.has(String(item.id)),
        type: item.type === 'warning' ? 'warning' : item.type === 'error' ? 'error' : item.type === 'success' ? 'success' : 'info',
        category: 'system',
      });
      if (Array.isArray(data)) {
        notifications.value = data.map(mapper);
      } else if (data.code === 1 && data.data) {
        notifications.value = data.data.map(mapper);
      }
    } catch (error) {
      console.error('Failed to load notifications:', error);
    }
  }

  function formatTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return '刚刚';
    if (minutes < 60) return `${minutes}分钟前`;
    if (hours < 24) return `${hours}小时前`;
    if (days < 7) return `${days}天前`;
    return date.toLocaleDateString();
  }

  const unreadCount = computed(() =>
    notifications.value.filter(n => !n.read).length
  );

  const unreadNotifications = computed(() =>
    notifications.value.filter(n => !n.read)
  );

  function addNotification(notification: Omit<AppNotification, 'id' | 'time' | 'read'>) {
    notifications.value.unshift({
      ...notification,
      id: `n${Date.now()}`,
      time: '刚刚',
      read: false,
    });
  }

  function markAsRead(id: string) {
    const n = notifications.value.find(n => n.id === id);
    if (n && !n.read) {
      n.read = true;
      readIds.value.add(id);
      saveReadIds(readIds.value);
    }
  }

  function markAllAsRead() {
    notifications.value.forEach(n => {
      n.read = true;
      readIds.value.add(n.id);
    });
    saveReadIds(readIds.value);
  }

  function removeNotification(id: string) {
    const index = notifications.value.findIndex(n => n.id === id);
    if (index > -1) {
      notifications.value.splice(index, 1);
    }
  }

  function clearAll() {
    notifications.value.forEach(n => readIds.value.add(n.id));
    saveReadIds(readIds.value);
    notifications.value = [];
  }

  return {
    notifications,
    unreadCount,
    unreadNotifications,
    loadNotifications,
    addNotification,
    markAsRead,
    markAllAsRead,
    removeNotification,
    clearAll,
  };
});
