import service from '@/api';

/**
 * ${entity.description!entity.name} API
 * 由低代码代码生成器生成 @ ${now}
 *
 * 后端路径：/lowcode/gen/${entity.code}
 * 前端路径前缀：/api（vite 代理）
 */
const ${entity.classNameLower}Api = {
  /** 分页查询列表 */
  list: (params: { pageNum: number; pageSize: number<#list searchFields as f>; ${f.camelName}?: ${f.tsType}</#list> }) =>
    service.get('/lowcode/gen/${entity.code}/list', { params }),

  /** 获取详情 */
  getById: (id: number) => service.get(`/lowcode/gen/${entity.code}/${r"${id}"}`),

  /** 新增 */
  create: (data: Record<string, any>) => service.post('/lowcode/gen/${entity.code}', data),

  /** 更新 */
  update: (id: number, data: Record<string, any>) =>
    service.put(`/lowcode/gen/${entity.code}/${r"${id}"}`, data),

  /** 删除 */
  delete: (id: number) => service.delete(`/lowcode/gen/${entity.code}/${r"${id}"}`),

  /** 批量删除 */
  deleteBatch: (ids: number[]) =>
    service.delete('/lowcode/gen/${entity.code}/batch', { data: ids }),
};

export default ${entity.classNameLower}Api;
