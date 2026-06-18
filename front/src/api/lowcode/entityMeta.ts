import service from '../index';
import type { EntityMeta, FieldMeta } from '../../types/lowcode';

const entityMetaApi = {
  /** 分页查询实体列表 */
  list: (params: {
    page: number;
    pageSize: number;
    keyword?: string;
    status?: string;
    sortBy?: string;
    sortOrder?: string;
  }) => service.get('/lowcode/entity/list', { params }),

  /** 获取实体详情（含字段） */
  getById: (id: number) => service.get(`/lowcode/entity/${id}`),

  /** 根据编码获取实体详情（含字段） */
  getByCode: (code: string) => service.get(`/lowcode/entity/code/${code}`),

  /** 创建实体 */
  create: (data: EntityMeta) => service.post('/lowcode/entity', data),

  /** 更新实体元数据 */
  update: (id: number, data: EntityMeta) => service.put(`/lowcode/entity/${id}`, data),

  /** 删除实体 */
  delete: (id: number) => service.delete(`/lowcode/entity/${id}`),

  /** 发布实体（创建物理表） */
  publish: (id: number) => service.post(`/lowcode/entity/${id}/publish`),

  /** 归档实体 */
  archive: (id: number) => service.post(`/lowcode/entity/${id}/archive`),

  /** 获取实体字段列表 */
  getFields: (entityId: number) => service.get(`/lowcode/entity/${entityId}/fields`),

  /** 更新实体字段（全量替换） */
  updateFields: (entityId: number, fields: FieldMeta[]) =>
    service.put(`/lowcode/entity/${entityId}/fields`, fields),
};

export default entityMetaApi;
