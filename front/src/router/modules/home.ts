import { DashboardIcon } from 'tdesign-icons-vue-next';
import { shallowRef } from 'vue';

import Layout from '../../layouts/index.vue';

export default [
    {
        path: '/',
        redirect: '/home',
    },
    {
        path: '/home',
        component: Layout,
        name: 'dashboard',
        meta: {
            title: {
                zh_CN: '首页',
            },
            icon: shallowRef(DashboardIcon),
            orderNo: 0,
        }
    },
];
