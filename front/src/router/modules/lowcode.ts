import { CodeIcon, LayoutIcon, HelpCircleIcon, AppIcon } from 'tdesign-icons-vue-next';
import { shallowRef } from 'vue';
import Layout from '../../layouts/index.vue';

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
        path: 'app/:appCode/menu',
        name: 'AppMenuEditor',
        component: () => import('../../pages/lowcode/app/AppMenuEditor.vue'),
        meta: { title: { zh_CN: '编辑应用菜单' }, hidden: true },
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
    ],
  },
];
