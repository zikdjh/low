import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export interface UserInfo {
  id: number;
  username: string;
  nickname: string;
  avatar?: string;
  email?: string;
  role: 'admin' | 'developer' | 'viewer';
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string | null>(localStorage.getItem('token'));
  const userInfo = ref<UserInfo | null>(null);
  const isLoggedIn = computed(() => !!token.value);
  const userRole = computed(() => userInfo.value?.role || 'viewer');
  const displayName = computed(() => userInfo.value?.nickname || userInfo.value?.username || '用户');

  // 设置 Token
  function setToken(newToken: string) {
    token.value = newToken;
    localStorage.setItem('token', newToken);
  }

  // 清除 Token
  function clearToken() {
    token.value = null;
    localStorage.removeItem('token');
    sessionStorage.removeItem('access');
  }

  // 设置用户信息
  function setUserInfo(info: UserInfo) {
    userInfo.value = info;
  }

  // 登录
  async function login(username: string, password: string): Promise<boolean> {
    try {
      // 注意：此处的登录逻辑需要与后端API对接
      // 目前仅做本地存储处理，实际应调用 /api/end/user/login
      setToken(`mock_token_${username}_${Date.now()}`);
      setUserInfo({
        id: 1,
        username,
        nickname: username,
        email: `${username}@example.com`,
        role: 'admin',
      });
      return true;
    } catch {
      return false;
    }
  }

  // 登出
  function logout() {
    clearToken();
    userInfo.value = null;
    window.location.href = '/login';
  }

  // 从localStorage恢复状态
  function restoreSession() {
    if (token.value && !userInfo.value) {
      // 尝试从本地恢复用户信息
      userInfo.value = {
        id: 1,
        username: 'admin',
        nickname: '管理员',
        email: 'admin@example.com',
        role: 'admin',
      };
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userRole,
    displayName,
    setToken,
    clearToken,
    setUserInfo,
    login,
    logout,
    restoreSession,
  };
});

export function getUserStore() {
  return useUserStore();
}
