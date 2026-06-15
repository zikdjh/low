import request from '../index';
import type { ComponentDef } from '../../types/lowcode';

interface ApiResponse<T> {
  code: number;
  data: T;
  message?: string;
}

export const componentDefApi = {
  getAllComponents: () => request.get<ApiResponse<ComponentDef[]>>('/lowcode/component'),
  
  getComponentsGrouped: () => request.get<ApiResponse<Record<string, ComponentDef[]>>>('/lowcode/component/grouped'),
  
  getComponentsByCategory: (category: string) => 
    request.get<ApiResponse<ComponentDef[]>>(`/lowcode/component/category/${category}`),
  
  getByCompKey: (compKey: string) => 
    request.get<ApiResponse<ComponentDef>>(`/lowcode/component/key/${compKey}`),
  
  getById: (id: number) => request.get<ComponentDef>(`/lowcode/component/${id}`),
  
  create: (data: ComponentDef) => request.post<ComponentDef>('/lowcode/component', data),
  
  update: (id: number, data: ComponentDef) => 
    request.put<ComponentDef>(`/lowcode/component/${id}`, data),
  
  delete: (id: number) => request.delete(`/lowcode/component/${id}`),
  
  batchSave: (data: ComponentDef[]) => 
    request.post('/lowcode/component/batch', data),
};