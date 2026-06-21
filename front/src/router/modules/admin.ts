import { SettingIcon } from 'tdesign-icons-vue-next';
import { shallowRef } from 'vue';

import AdminLayout from '../../layouts/admin/index.vue';

export default [
  {
    path: '/admin/login',
    name: 'adminLogin',
    component: () => import('../../views/admin/Login.vue'),
    meta: {
      title: { zh_CN: '管理员登录' },
      admin: true,
    },
  },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/users',
    meta: {
      title: { zh_CN: '管理后台' },
      icon: shallowRef(SettingIcon),
      orderNo: 200,
      admin: true,
    },
    children: [
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../../views/admin/Users.vue'),
        meta: { title: { zh_CN: '用户管理' }, admin: true },
      },
      {
        path: 'pages',
        name: 'AdminPages',
        component: () => import('../../views/admin/Pages.vue'),
        meta: { title: { zh_CN: '页面管理' }, admin: true },
      },
      {
        path: 'entities',
        name: 'AdminEntities',
        component: () => import('../../views/admin/Entities.vue'),
        meta: { title: { zh_CN: '实体管理' }, admin: true },
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('../../views/admin/Profile.vue'),
        meta: { title: { zh_CN: '个人信息' }, admin: true },
      },
    ],
  },
];
