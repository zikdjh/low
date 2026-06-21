import service from '../index';
import type { AxiosResponse } from 'axios';

/** 统一抽取 response.data，与 authApi 保持一致 */
function extract<T = any>(resp: AxiosResponse<T>): T {
  return resp.data;
}

/** 管理员登录（复用 /auth/login） */
export const adminAuthApi = {
  login: async (data: { username: string; password: string }) =>
    extract(await service.post('/auth/login', data)),
  logout: async () =>
    extract(await service.post('/auth/logout')),
};

/** 管理端业务 API */
export const adminApi = {
  /** 用户列表 */
  listUsers: async () => extract(await service.get('/admin/users')),
  /** 冻结用户 */
  freezeUser: async (id: number) => extract(await service.put(`/admin/users/${id}/freeze`)),
  /** 解冻用户 */
  unfreezeUser: async (id: number) => extract(await service.put(`/admin/users/${id}/unfreeze`)),
  /** 所有页面 */
  listPages: async () => extract(await service.get('/admin/pages')),
  /** 所有实体 */
  listEntities: async () => extract(await service.get('/admin/entities')),
  /** 获取个人信息 */
  getProfile: async () => extract(await service.get('/admin/profile')),
  /** 更新个人信息 */
  updateProfile: async (data: Record<string, string>) =>
    extract(await service.put('/admin/profile', data)),
};

export default adminApi;
