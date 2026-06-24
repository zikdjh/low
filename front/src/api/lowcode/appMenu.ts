import request from '../index';

/**
 * 应用菜单 API（M2 后端）
 *
 * 端点约定（与 back/src/main/java/com/back/lowcode/lcmenu/MenuController 对齐）：
 *   GET    /lowcode/app/{appCode}/menu            读 active release 菜单（终端用户）
 *   GET    /lowcode/app/{appCode}/menu/release/:rid  读指定 release 菜单（历史回看）
 *   GET    /lowcode/app/{appCode}/menu/draft      读草稿菜单（设计器）
 *   PUT    /lowcode/app/{appCode}/menu/draft      全量保存草稿菜单
 *   POST   /lowcode/app/{appCode}/menu/reorder    批量调整 parentId / sortOrder
 */

export interface MenuNode {
  id?: number;
  appCode?: string;
  parentId?: number | null;
  name: string;
  icon?: string;
  sortOrder?: number;
  /** 'group' 仅做分组，无 pageCode；'menu' 指向页面 */
  menuType?: 'group' | 'menu';
  pageCode?: string;
  routePath?: string;
  visible?: boolean;
  children?: MenuNode[];
}

export interface ReorderItem {
  id: number;
  parentId: number | null;
  sortOrder: number;
}

interface ApiResponse<T> {
  code: number;
  data: T;
  msg?: string;
}

export const appMenuApi = {
  /** active release 的菜单树（运行时） */
  getActive: (appCode: string) =>
    request.get<ApiResponse<MenuNode[]>>(`/lowcode/app/${appCode}/menu`),

  /** 指定 release 的菜单（历史预览） */
  getByRelease: (appCode: string, releaseId: number) =>
    request.get<ApiResponse<MenuNode[]>>(`/lowcode/app/${appCode}/menu/release/${releaseId}`),

  /** 草稿菜单（设计器） */
  getDraft: (appCode: string) =>
    request.get<ApiResponse<MenuNode[]>>(`/lowcode/app/${appCode}/menu/draft`),

  /** 全量保存草稿菜单（PUT 语义） */
  saveDraft: (appCode: string, tree: MenuNode[]) =>
    request.put<ApiResponse<MenuNode[]>>(`/lowcode/app/${appCode}/menu/draft`, tree),

  /** 拖拽局部排序 */
  reorder: (appCode: string, items: ReorderItem[]) =>
    request.post<ApiResponse<void>>(`/lowcode/app/${appCode}/menu/reorder`, items),
};
