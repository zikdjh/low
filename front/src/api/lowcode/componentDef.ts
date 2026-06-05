import request from '../index';
import type { ComponentDef } from '../../types/lowcode';

export const componentDefApi = {
  getAllComponents: () => request.get<ComponentDef[]>('/lowcode/component'),
  
  getComponentsGrouped: () => request.get<Record<string, ComponentDef[]>>('/lowcode/component/grouped'),
  
  getComponentsByCategory: (category: string) => 
    request.get<ComponentDef[]>(`/lowcode/component/category/${category}`),
  
  getByCompKey: (compKey: string) => 
    request.get<ComponentDef>(`/lowcode/component/key/${compKey}`),
  
  getById: (id: number) => request.get<ComponentDef>(`/lowcode/component/${id}`),
  
  create: (data: ComponentDef) => request.post<ComponentDef>('/lowcode/component', data),
  
  update: (id: number, data: ComponentDef) => 
    request.put<ComponentDef>(`/lowcode/component/${id}`, data),
  
  delete: (id: number) => request.delete(`/lowcode/component/${id}`),
  
  batchSave: (data: ComponentDef[]) => 
    request.post('/lowcode/component/batch', data),
};