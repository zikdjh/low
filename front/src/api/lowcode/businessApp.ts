import request from '../index';

export interface BusinessApp {
  id?: number;
  name: string;
  code: string;
  description?: string;
  icon?: string;
  color?: string;
  createdAt?: string;
  updatedAt?: string;
}

interface ApiResponse<T> {
  code: number;
  data: T;
  message?: string;
}

export const businessAppApi = {
  getAll: () => request.get<ApiResponse<BusinessApp[]>>('/lowcode/app'),

  getById: (id: number) => request.get<ApiResponse<BusinessApp>>(`/lowcode/app/${id}`),

  getByCode: (code: string) => request.get<ApiResponse<BusinessApp>>(`/lowcode/app/code/${code}`),

  getPages: (appCode: string) => request.get<ApiResponse<any[]>>(`/lowcode/app/${appCode}/pages`),

  create: (data: BusinessApp) => request.post<BusinessApp>('/lowcode/app', data),

  update: (id: number, data: BusinessApp) =>
    request.put<BusinessApp>(`/lowcode/app/${id}`, data),

  delete: (id: number) => request.delete(`/lowcode/app/${id}`),

  assignPage: (appCode: string, pageId: number) =>
    request.post(`/lowcode/app/${appCode}/assign/${pageId}`),

  /** 批量将多个页面聚合到某个业务应用 */
  assignBatchPages: (appCode: string, pageIds: number[]) =>
    request.post(`/lowcode/app/${appCode}/assign-batch`, { pageIds }),
};
