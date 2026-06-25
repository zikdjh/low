import service from '../index';

/**
 * ${entity.description!entity.name} API
 * 由低代码代码生成器生成 @ ${now}
 */
const ${entity.classNameLower}Api = {
  /** 分页查询列表 */
  list: (params: { pageNum: number; pageSize: number<#list searchFields as f>; ${f.camelName}?: ${f.tsType}</#list> }) =>
    service.get('/${entity.classNameLower}/list', { params }),

  /** 获取详情 */
  getById: (id: number) => service.get(`/${entity.classNameLower}/${r"${id}"}`),

  /** 新增 */
  create: (data: Record<string, any>) => service.post('/${entity.classNameLower}', data),

  /** 更新 */
  update: (id: number, data: Record<string, any>) =>
    service.put(`/${entity.classNameLower}/${r"${id}"}`, data),

  /** 删除 */
  delete: (id: number) => service.delete(`/${entity.classNameLower}/${r"${id}"}`),

  /** 批量删除 */
  deleteBatch: (ids: number[]) => service.delete('/${entity.classNameLower}/batch', { data: ids }),
};

export default ${entity.classNameLower}Api;
