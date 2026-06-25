import { CodeIcon, LayoutIcon, HelpCircleIcon, AppIcon } from 'tdesign-icons-vue-next';
import { shallowRef } from 'vue';
import Layout from '../../layouts/index.vue';

/**
 * 自动发现 src/pages/lowcode/generated/ 下的代码生成产物。
 *
 * 约定：每个实体对应两个文件 {ClassName}List.vue 和 {ClassName}Edit.vue。
 * 由 import.meta.glob 在编译期扫描——新生成文件后重启 dev server / 重新 build 才会被发现。
 */
const generatedListModules = import.meta.glob('../../pages/lowcode/generated/*List.vue');
const generatedEditModules = import.meta.glob('../../pages/lowcode/generated/*Edit.vue');

/** 从文件路径里抽出 ClassName，例如 "../../pages/lowcode/generated/CustomerList.vue" -> "Customer" */
function extractClassName(path: string, suffix: 'List' | 'Edit'): string | null {
  const m = path.match(new RegExp(`/([A-Z][A-Za-z0-9]*)${suffix}\\.vue$`));
  return m ? m[1] : null;
}

/** className (PascalCase) -> entityCode (snake_case)，与后端 CodeGenService.toCamelCase 反向。 */
function classNameToEntityCode(className: string): string {
  return className.replace(/([A-Z])/g, (_, c, i) => (i === 0 ? c.toLowerCase() : '_' + c.toLowerCase()));
}

const generatedRoutes: any[] = [];
for (const [path, loader] of Object.entries(generatedListModules)) {
  const className = extractClassName(path, 'List');
  if (!className) continue;
  const entityCode = classNameToEntityCode(className);
  generatedRoutes.push({
    path: `gen/${entityCode}/list`,
    name: `Gen_${className}_List`,
    component: loader as any,
    meta: { title: { zh_CN: `${className} 列表` }, hidden: true, generated: true },
  });
}
for (const [path, loader] of Object.entries(generatedEditModules)) {
  const className = extractClassName(path, 'Edit');
  if (!className) continue;
  const entityCode = classNameToEntityCode(className);
  generatedRoutes.push({
    path: `gen/${entityCode}/edit/:id`,
    name: `Gen_${className}_Edit`,
    component: loader as any,
    meta: { title: { zh_CN: `${className} 编辑` }, hidden: true, generated: true },
  });
}

export default [
  {
    path: '/lowcode',
    component: Layout,
    redirect: '/lowcode/entity',
    name: 'lowcode',
    meta: {
      title: { zh_CN: '低代码平台' },
      icon: shallowRef(CodeIcon),
      orderNo: 100,
    },
    children: [
      {
        path: 'app',
        name: 'BusinessAppList',
        component: () => import('../../pages/lowcode/app/BusinessAppPage.vue'),
        meta: { title: { zh_CN: '业务应用' }, icon: shallowRef(AppIcon), hidden: true },
      },
      {
        path: 'app/:appCode',
        name: 'BusinessAppDetail',
        component: () => import('../../pages/lowcode/app/BusinessAppPage.vue'),
        meta: { title: { zh_CN: '业务应用详情' }, hidden: true },
      },
      {
        path: 'entity',
        name: 'EntityList',
        component: () => import('../../pages/lowcode/metadata/EntityList.vue'),
        meta: { title: { zh_CN: '实体管理' } },
      },
      {
        path: 'entity/:id',
        name: 'EntityEdit',
        component: () => import('../../pages/lowcode/metadata/EntityEdit.vue'),
        meta: { title: { zh_CN: '实体编辑' }, hidden: true },
      },
      {
        path: 'entity/:id/view',
        name: 'EntityView',
        component: () => import('../../pages/lowcode/metadata/EntityView.vue'),
        meta: { title: { zh_CN: '实体详情' }, hidden: true },
      },
      {
        path: 'entity/:id/codegen',
        name: 'CodeGenPage',
        component: () => import('../../pages/lowcode/codegen/CodeGenPage.vue'),
        meta: { title: { zh_CN: '代码生成' }, hidden: true },
      },
      {
        path: 'entity/relations',
        name: 'EntityRelationList',
        component: () => import('../../pages/lowcode/metadata/EntityRelationList.vue'),
        meta: { title: { zh_CN: '实体关系' }, hidden: true },
      },
      {
        path: 'data/:entityCode',
        name: 'DataList',
        component: () => import('../../pages/lowcode/data/DataList.vue'),
        meta: { title: { zh_CN: '数据管理' }, hidden: true },
      },
      {
        path: 'page/list',
        name: 'PageList',
        component: () => import('../../pages/lowcode/page/PageList.vue'),
        meta: { title: { zh_CN: '页面管理' }, icon: shallowRef(LayoutIcon) },
      },
      {
        path: 'page/design',
        name: 'PageDesigner',
        component: () => import('../../pages/lowcode/page/PageDesigner.vue'),
        meta: { title: { zh_CN: '页面设计' }, hidden: true },
      },
      {
        path: 'page/view',
        name: 'PageViewer',
        component: () => import('../../pages/lowcode/page/PageViewer.vue'),
        meta: { title: { zh_CN: '页面预览' }, hidden: true },
      },
      {
        path: 'notification',
        name: 'NotificationCenter',
        component: () => import('../../pages/notification/index.vue'),
        meta: { title: { zh_CN: '通知中心' }, hidden: true },
      },
      {
        path: 'settings',
        name: 'SettingsPage',
        component: () => import('../../pages/settings/index.vue'),
        meta: { title: { zh_CN: '系统设置' }, hidden: true },
      },
      {
        path: 'page/preview/:pageCode',
        name: 'SchemaRenderer',
        component: () => import('../../pages/lowcode/SchemaRenderer.vue'),
        meta: { title: { zh_CN: '页面预览' }, hidden: true },
      },
      {
        path: 'dict',
        name: 'DictList',
        component: () => import('../../pages/lowcode/metadata/DictList.vue'),
        meta: { title: { zh_CN: '字典管理' }, icon: shallowRef(CodeIcon) },
      },
      {
        path: 'help',
        name: 'HelpCenter',
        component: () => import('../../pages/help/index.vue'),
        meta: { title: { zh_CN: '帮助中心' }, icon: shallowRef(HelpCircleIcon), hidden: true },
      },
      // 代码生成产物 —— 由 import.meta.glob 编译期注入
      ...generatedRoutes,
    ],
  },
];
