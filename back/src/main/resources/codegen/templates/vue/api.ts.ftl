<#--
  axios API 客户端模板 —— 由低代码平台 release 快照生成
  期望根上下文：entity，appCode
  说明：生成产物默认使用裸 axios + baseURL=/api，落地到目标工程后可改为该工程的统一 request 包装。
-->
import axios from 'axios';

const request = axios.create({ baseURL: '/api', timeout: 10000 });

export interface ${entity.className} {
<#list entity.fields as f>
  ${f.camelName}<#if f.nullable>?</#if>: ${f.tsType};
</#list>
}

export const ${entity.varName}Api = {
  /** 新增 */
  create(body: ${entity.className}) {
    return request.post(`/${appCode}/${entity.code}`, body);
  },
  /** 更新 */
  update(id: ${entity.primaryKeyField.tsType!'number'}, body: ${entity.className}) {
    return request.put(`/${appCode}/${entity.code}/${r"${"}id}`, body);
  },
  /** 详情 */
  get(id: ${entity.primaryKeyField.tsType!'number'}) {
    return request.get(`/${appCode}/${entity.code}/${r"${"}id}`);
  },
  /** 删除 */
  remove(id: ${entity.primaryKeyField.tsType!'number'}) {
    return request.delete(`/${appCode}/${entity.code}/${r"${"}id}`);
  },
  /** 全量 */
  list() {
    return request.get(`/${appCode}/${entity.code}`);
  },
  /** 分页 */
  page(params: { page: number; size: number }) {
    return request.get(`/${appCode}/${entity.code}/page`, { params });
  },
};
