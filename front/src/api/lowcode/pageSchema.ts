import request from '../index';
import type { PageSchema } from '../../types/lowcode';

interface ApiResponse<T> {
  code: number;
  data: T;
  message?: string;
}

export const pageSchemaApi = {
  getAllPages: () => request.get<ApiResponse<PageSchema[]>>('/lowcode/page'),
  
  getPagesByStatus: (status: string) => 
    request.get<ApiResponse<PageSchema[]>>(`/lowcode/page/status/${status}`),
  
  getByPageCode: (pageCode: string) => 
    request.get<ApiResponse<PageSchema>>(`/lowcode/page/code/${pageCode}`),
  
  getById: (id: number) => request.get<ApiResponse<PageSchema>>(`/lowcode/page/${id}`),
  
  create: (data: PageSchema) => request.post<PageSchema>('/lowcode/page', data),
  
  update: (id: number, data: PageSchema) => 
    request.put<PageSchema>(`/lowcode/page/${id}`, data),
  
  publish: (id: number) => request.post<PageSchema>(`/lowcode/page/${id}/publish`),
  
  unpublish: (id: number) => request.post<PageSchema>(`/lowcode/page/${id}/unpublish`),
  
  delete: (id: number) => request.delete(`/lowcode/page/${id}`),
};