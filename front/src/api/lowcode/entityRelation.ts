import request from '@/utils/request';
import type { EntityRelation } from '@/types/lowcode';

const entityRelationApi = {
  listAll() {
    return request.get('/lowcode/relation');
  },

  listEnabled() {
    return request.get('/lowcode/relation/enabled');
  },

  getById(id: number) {
    return request.get(`/lowcode/relation/${id}`);
  },

  getBySourceEntity(entityCode: string) {
    return request.get(`/lowcode/relation/source/${entityCode}`);
  },

  getByTargetEntity(entityCode: string) {
    return request.get(`/lowcode/relation/target/${entityCode}`);
  },

  getByEntity(entityCode: string) {
    return request.get(`/lowcode/relation/entity/${entityCode}`);
  },

  create(data: EntityRelation) {
    return request.post('/lowcode/relation', data);
  },

  update(id: number, data: EntityRelation) {
    return request.put(`/lowcode/relation/${id}`, data);
  },

  delete(id: number) {
    return request.delete(`/lowcode/relation/${id}`);
  },

  enable(id: number) {
    return request.post(`/lowcode/relation/${id}/enable`);
  },

  disable(id: number) {
    return request.post(`/lowcode/relation/${id}/disable`);
  },
};

export default entityRelationApi;
