import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import authApi from '../../api/auth';
import type { AuthUserView, LoginPayload } from '../../api/model/user/Auth.ts';

export interface UserInfo {
  id: number;
  username: string;
  nickname?: string;
  avatar?: string;
  email?: string;
  /** 角色编码列表，与后端 lc_role.code 对齐，例如 ['user'] / ['root'] */
  roles: string[];
  createdAt?: string;
}

const USER_INFO_KEY = 'userInfo';
const ACCESS_KEY = 'access';
/** 与后端 auth.jwt.access-expire-minutes 对齐 */
const ACCESS_TTL_MS = 45 * 60 * 1000;

function readUserInfo(): UserInfo | null {
  const raw = localStorage.getItem(USER_INFO_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as UserInfo;
  } catch {
    return null;
  }
}

function writeAccessToken(token: string) {
  sessionStorage.setItem(
      ACCESS_KEY,
      JSON.stringify({ token, expiresAt: Date.now() + ACCESS_TTL_MS })
  );
}

function clearAccessToken() {
  sessionStorage.removeItem(ACCESS_KEY);
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const userInfo = ref<UserInfo | null>(readUserInfo());
  const isLoggedIn = computed(() => !!userInfo.value);
  const roles = computed<string[]>(() => userInfo.value?.roles ?? []);
  const isRoot = computed(() => roles.value.includes('root'));
  const isAdmin = computed(() => isRoot.value || roles.value.includes('admin'));
  /** 主角色（用于一些需要单值的旧调用），优先级 root > admin > user */
  const primaryRole = computed<string>(() => {
    if (isRoot.value) return 'root';
    if (roles.value.includes('admin')) return 'admin';
    if (roles.value.includes('user')) return 'user';
    return roles.value[0] ?? 'user';
  });
  // 兼容旧字段名 userRole
  const userRole = primaryRole;
  const displayName = computed(
      () => userInfo.value?.nickname || userInfo.value?.username || '用户'
  );

  /** 写入登录态（用户信息 + access token） */
  function applyLoginPayload(payload: LoginPayload | undefined | null) {
    if (!payload) return;
    if (payload.token) writeAccessToken(payload.token);
    setUserInfo(toUserInfo(payload.user));
  }

  function setUserInfo(info: UserInfo | null) {
    userInfo.value = info;
    if (info) {
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(info));
    } else {
      localStorage.removeItem(USER_INFO_KEY);
    }
  }

  // 注册：成功后立即写入登录态（含 access token）
  async function register(payload: {
    username: string;
    password: string;
    nickname?: string;
  }): Promise<{ ok: boolean; msg?: string }> {
    const res = await authApi.register(payload);
    if (res.code === 1) {
      applyLoginPayload(res.data);
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
      applyLoginPayload(res.data);
      return { ok: true };
    }
    return { ok: false, msg: res.msg };
  }

  // 登出：调后端清 refresh Cookie，再清前端缓存
  async function logout(redirect: boolean = true) {
    try {
      await authApi.logout();
    } catch {
      // 即便服务端清理失败，本地仍要登出
    }
    setUserInfo(null);
    clearAccessToken();
    // 兼容遗留 key
    localStorage.removeItem('token');
    if (redirect) {
      window.location.href = '/login';
    }
  }

  /** 兼容旧调用方：从 localStorage 拉回内存态 */
  function restoreSession() {
    if (!userInfo.value) {
      const cached = readUserInfo();
      if (cached) userInfo.value = cached;
    }
  }

  return {
    userInfo,
    isLoggedIn,
    roles,
    isAdmin,
    isRoot,
    primaryRole,
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
    roles: view?.roles ?? [],
  };
}

export function getUserStore() {
  return useUserStore();
}
