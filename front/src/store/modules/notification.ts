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

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<AppNotification[]>([]);

  async function loadNotifications() {
    try {
      const response = await notificationApi.getPublishedNotifications();
      const data = response.data || response;
      if (Array.isArray(data)) {
        notifications.value = data.map((item: any) => ({
          id: String(item.id),
          title: item.title,
          description: item.content,
          time: formatTime(item.publishedAt || item.createdAt),
          read: false,
          type: item.type === 'warning' ? 'warning' : item.type === 'error' ? 'error' : item.type === 'success' ? 'success' : 'info',
          category: 'system',
        }));
      } else if (data.code === 1 && data.data) {
        notifications.value = data.data.map((item: any) => ({
          id: String(item.id),
          title: item.title,
          description: item.content,
          time: formatTime(item.publishedAt || item.createdAt),
          read: false,
          type: item.type === 'warning' ? 'warning' : item.type === 'error' ? 'error' : item.type === 'success' ? 'success' : 'info',
          category: 'system',
        }));
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
    }
  }

  function markAllAsRead() {
    notifications.value.forEach(n => (n.read = true));
  }

  function removeNotification(id: string) {
    const index = notifications.value.findIndex(n => n.id === id);
    if (index > -1) {
      notifications.value.splice(index, 1);
    }
  }

  function clearAll() {
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
