import request from '../index';
import type { PageSchema } from '../../types/lowcode';

interface ApiResponse<T> {
  code: number;
  data: T;
  message?: string;
}

export const pageSchemaApi = {
  getAllPages: () => request.get<ApiResponse<PageSchema[]>>('/lowcode/page'),
  
  getPagesByStatus: (status: string) => 
    request.get<ApiResponse<PageSchema[]>>(`/lowcode/page/status/${status}`),
  
  getByPageCode: (pageCode: string) => 
    request.get<ApiResponse<PageSchema>>(`/lowcode/page/code/${pageCode}`),
  
  getById: (id: number) => request.get<ApiResponse<PageSchema>>(`/lowcode/page/${id}`),
  
  create: (data: PageSchema) => request.post<PageSchema>('/lowcode/page', data),
  
  update: (id: number, data: PageSchema) => 
    request.put<PageSchema>(`/lowcode/page/${id}`, data),
  
  publish: (id: number) => request.post<PageSchema>(`/lowcode/page/${id}/publish`),
  
  unpublish: (id: number) => request.post<PageSchema>(`/lowcode/page/${id}/unpublish`),
  
  delete: (id: number) => request.delete(`/lowcode/page/${id}`),

  /** 下载页面为 Vue 文件 */
  downloadVueFile: async (id: number, pageCode: string): Promise<void> => {
    const res = await request.get(`/lowcode/page/${id}/download`, { responseType: 'blob' });
    const blob = new Blob([res.data], { type: 'application/octet-stream' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${pageCode}.vue`;
    a.click();
    window.URL.revokeObjectURL(url);
  },
};