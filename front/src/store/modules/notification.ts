import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

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
  const notifications = ref<AppNotification[]>([
    {
      id: 'n1',
      title: '系统更新',
      description: '低代码平台已更新至 v2.0.0，全新主题界面上线',
      time: '5分钟前',
      read: false,
      type: 'system',
      category: 'system',
    },
    {
      id: 'n2',
      title: '任务完成',
      description: '您创建的页面"首页"已成功发布',
      time: '15分钟前',
      read: false,
      type: 'success',
      category: 'task',
    },
    {
      id: 'n3',
      title: '新消息',
      description: '团队成员邀请您协作编辑页面',
      time: '1小时前',
      read: false,
      type: 'info',
      category: 'team',
    },
    {
      id: 'n4',
      title: '数据异常提醒',
      description: '实体"用户"的数据量已超过 10,000 条，建议进行分页优化',
      time: '2小时前',
      read: true,
      type: 'warning',
      category: 'system',
    },
    {
      id: 'n5',
      title: '构建完成',
      description: '项目构建成功，耗时 2.3s',
      time: '3小时前',
      read: true,
      type: 'success',
      category: 'system',
    },
    {
      id: 'n6',
      title: '权限变更',
      description: '您的角色已更新为"系统管理员"',
      time: '昨天',
      read: true,
      type: 'system',
      category: 'system',
    },
    {
      id: 'n7',
      title: '页面访问统计',
      description: '上周"首页"页面访问量达到 1,200 次',
      time: '昨天',
      read: true,
      type: 'info',
      category: 'task',
    },
  ]);

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
    addNotification,
    markAsRead,
    markAllAsRead,
    removeNotification,
    clearAll,
  };
});
