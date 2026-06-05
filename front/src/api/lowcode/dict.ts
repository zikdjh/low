import request from '../index';
import type { DictType, DictItem } from '../../types/lowcode';

export const dictApi = {
  getAllDictTypes: () => request.get<DictType[]>('/lowcode/dict/types'),
  
  getDictType: (dictCode: string) => 
    request.get<DictType>(`/lowcode/dict/types/${dictCode}`),
  
  createDictType: (data: DictType) => 
    request.post<DictType>('/lowcode/dict/types', data),
  
  updateDictType: (id: number, data: DictType) => 
    request.put<DictType>(`/lowcode/dict/types/${id}`, data),
  
  deleteDictType: (id: number) => request.delete(`/lowcode/dict/types/${id}`),
  
  getDictItems: (dictCode: string) => 
    request.get<DictItem[]>(`/lowcode/dict/items/${dictCode}`),
  
  getDictMap: (dictCode: string) => 
    request.get<Record<string, string>>(`/lowcode/dict/map/${dictCode}`),
  
  createDictItem: (data: DictItem) => 
    request.post<DictItem>('/lowcode/dict/items', data),
  
  updateDictItem: (id: number, data: DictItem) => 
    request.put<DictItem>(`/lowcode/dict/items/${id}`, data),
  
  deleteDictItem: (id: number) => request.delete(`/lowcode/dict/items/${id}`),
  
  batchSaveItems: (dictCode: string, items: DictItem[]) => 
    request.post(`/lowcode/dict/items/batch/${dictCode}`, items),
};