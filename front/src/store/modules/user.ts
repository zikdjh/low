import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import authApi from '../../api/auth';
import type { AuthUserView } from '../../api/model/user/Auth.ts';

export interface UserInfo {
  id: number;
  username: string;
  nickname?: string;
  avatar?: string;
  email?: string;
  role?: 'admin' | 'developer' | 'viewer';
  createdAt?: string;
}

const USER_INFO_KEY = 'userInfo';

function readUserInfo(): UserInfo | null {
  const raw = localStorage.getItem(USER_INFO_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as UserInfo;
  } catch {
    return null;
  }
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const userInfo = ref<UserInfo | null>(readUserInfo());
  const isLoggedIn = computed(() => !!userInfo.value);
  const userRole = computed(() => userInfo.value?.role || 'viewer');
  const displayName = computed(
      () => userInfo.value?.nickname || userInfo.value?.username || '用户'
  );

  // 设置用户信息（同步到 localStorage，刷新后保持登录态）
  function setUserInfo(info: UserInfo | null) {
    userInfo.value = info;
    if (info) {
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(info));
    } else {
      localStorage.removeItem(USER_INFO_KEY);
    }
  }

  // 注册：成功后立即写入登录态
  async function register(payload: {
    username: string;
    password: string;
    nickname?: string;
  }): Promise<{ ok: boolean; msg?: string }> {
    const res = await authApi.register(payload);
    if (res.code === 1) {
      setUserInfo(toUserInfo(res.data));
      return { ok: true };
    }
    return { ok: false, msg: res.msg };
  }

  // 登录
  async function loginByPassword(payload: {
    username: string;
    password: string;
  }): Promise<{ ok: boolean; msg?: string }> {
    const res = await authApi.login(payload);
    if (res.code === 1) {
      setUserInfo(toUserInfo(res.data));
      return { ok: true };
    }
    return { ok: false, msg: res.msg };
  }

  // 登出
  function logout() {
    setUserInfo(null);
    sessionStorage.removeItem('access');
    localStorage.removeItem('token');
    window.location.href = '/login';
  }

  // 兼容：旧调用方仍会触发 restoreSession，这里做成幂等空操作
  function restoreSession() {
    if (!userInfo.value) {
      const cached = readUserInfo();
      if (cached) userInfo.value = cached;
    }
  }

  return {
    userInfo,
    isLoggedIn,
    userRole,
    displayName,
    setUserInfo,
    register,
    loginByPassword,
    logout,
    restoreSession,
  };
});

function toUserInfo(view: AuthUserView | undefined | null): UserInfo {
  return {
    id: view?.id ?? 0,
    username: view?.username ?? '',
    nickname: view?.nickname,
    createdAt: view?.createdAt,
    role: 'admin', // 后端尚未返回角色，默认 admin 以放行所有页面
  };
}

export function getUserStore() {
  return useUserStore();
}
