import service from '../index';

/** 单个生成文件的内容 (path -> content) */
export interface CodeGenPreview {
  entityCode: string;
  entityName: string;
  className: string;
  files: Record<string, string>;
}

const codeGenApi = {
  /** 预览指定实体的代码生成结果 */
  preview: (entityId: number) =>
    service.get(`/lowcode/codegen/preview/${entityId}`) as Promise<{
      data: { code: number; msg: string; data: CodeGenPreview };
    }>,

  /** 下载 zip — 直接拼接 URL 让浏览器以 GET 触发下载（会带 access token cookie 不够，所以仍通过 axios 拿 blob） */
  download: (entityId: number) =>
    service.get(`/lowcode/codegen/download/${entityId}`, {
      responseType: 'blob',
    }),

  /** 安装到当前项目源码目录（重启后端 + 重启前端 dev server 后生效） */
  install: (entityId: number, force = false) =>
    service.post(`/lowcode/codegen/install/${entityId}`, null, { params: { force } }),
};

export default codeGenApi;
