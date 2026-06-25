import service from '@/api';

/**
 * 学生基本信息，包含学号、班级、院系、联系方式等 API
 * 由低代码代码生成器生成 @ 2026-06-25T21:02:06.2970011
 *
 * 后端路径：/lowcode/gen/student
 * 前端路径前缀：/api（vite 代理）
 */
const studentApi = {
  /** 分页查询列表 */
  list: (params: { pageNum: number; pageSize: number; studentNo?: string; name?: string; className?: string; status?: string }) =>
    service.get('/lowcode/gen/student/list', { params }),

  /** 获取详情 */
  getById: (id: number) => service.get(`/lowcode/gen/student/${id}`),

  /** 新增 */
  create: (data: Record<string, any>) => service.post('/lowcode/gen/student', data),

  /** 更新 */
  update: (id: number, data: Record<string, any>) =>
    service.put(`/lowcode/gen/student/${id}`, data),

  /** 删除 */
  delete: (id: number) => service.delete(`/lowcode/gen/student/${id}`),

  /** 批量删除 */
  deleteBatch: (ids: number[]) =>
    service.delete('/lowcode/gen/student/batch', { data: ids }),
};

export default studentApi;
