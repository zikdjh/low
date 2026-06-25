import service from '@/api';

/**
 * 学生请假申请记录，关联学生实体，通过工作流完成审批流程 API
 * 由低代码代码生成器生成 @ 2026-06-25T20:49:37.0509793
 *
 * 后端路径：/lowcode/gen/leave_application
 * 前端路径前缀：/api（vite 代理）
 */
const leaveApplicationApi = {
  /** 分页查询列表 */
  list: (params: { pageNum: number; pageSize: number; studentId?: number; studentName?: string; leaveType?: string; status?: string }) =>
    service.get('/lowcode/gen/leave_application/list', { params }),

  /** 获取详情 */
  getById: (id: number) => service.get(`/lowcode/gen/leave_application/${id}`),

  /** 新增 */
  create: (data: Record<string, any>) => service.post('/lowcode/gen/leave_application', data),

  /** 更新 */
  update: (id: number, data: Record<string, any>) =>
    service.put(`/lowcode/gen/leave_application/${id}`, data),

  /** 删除 */
  delete: (id: number) => service.delete(`/lowcode/gen/leave_application/${id}`),

  /** 批量删除 */
  deleteBatch: (ids: number[]) =>
    service.delete('/lowcode/gen/leave_application/batch', { data: ids }),
};

export default leaveApplicationApi;
