import { CodeIcon, LayoutIcon } from 'tdesign-icons-vue-next';
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
    ],
  },
];
