import request from '../index';
import type { PageSchema } from '../../types/lowcode';

export const pageSchemaApi = {
  getAllPages: () => request.get<PageSchema[]>('/lowcode/page'),
  
  getPagesByStatus: (status: string) => 
    request.get<PageSchema[]>(`/lowcode/page/status/${status}`),
  
  getByPageCode: (pageCode: string) => 
    request.get<PageSchema>(`/lowcode/page/code/${pageCode}`),
  
  getById: (id: number) => request.get<PageSchema>(`/lowcode/page/${id}`),
  
  create: (data: PageSchema) => request.post<PageSchema>('/lowcode/page', data),
  
  update: (id: number, data: PageSchema) => 
    request.put<PageSchema>(`/lowcode/page/${id}`, data),
  
  publish: (id: number) => request.post<PageSchema>(`/lowcode/page/${id}/publish`),
  
  unpublish: (id: number) => request.post<PageSchema>(`/lowcode/page/${id}/unpublish`),
  
  delete: (id: number) => request.delete(`/lowcode/page/${id}`),
};