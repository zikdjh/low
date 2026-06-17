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
        path: 'page',
        name: 'PageDesigner',
        component: () => import('../../pages/lowcode/page/PageDesigner.vue'),
        meta: { title: { zh_CN: '页面设计' }, icon: shallowRef(LayoutIcon) },
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
    ],
  },
];
