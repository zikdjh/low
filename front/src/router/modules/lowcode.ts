import { CodeIcon } from 'tdesign-icons-vue-next';
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
    ],
  },
];
