import service from '../index';
import type { GenericQueryRequest } from '../../types/lowcode';

const dynamicDataApi = {
  list: (entityCode: string, params: Partial<GenericQueryRequest>) =>
    service.get(`/lowcode/data/${entityCode}`, { params }),

  getById: (entityCode: string, id: number | string) =>
    service.get(`/lowcode/data/${entityCode}/${id}`),

  create: (entityCode: string, data: Record<string, any>) =>
    service.post(`/lowcode/data/${entityCode}`, data),

  update: (entityCode: string, id: number | string, data: Record<string, any>) =>
    service.put(`/lowcode/data/${entityCode}/${id}`, data),

  delete: (entityCode: string, id: number | string) =>
    service.delete(`/lowcode/data/${entityCode}/${id}`),

  batchDelete: (entityCode: string, ids: (number | string)[]) =>
    service.post(`/lowcode/data/${entityCode}/batch-delete`, { ids }),
};

export default dynamicDataApi;
