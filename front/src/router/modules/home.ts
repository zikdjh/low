import { DashboardIcon, SettingIcon } from 'tdesign-icons-vue-next';
import { shallowRef } from 'vue';

import Layout from '../../layouts/index.vue';
import BlankLayout from '../../layouts/blank.vue';
import HomePage from '../../pages/home/index.vue';
import SettingPage from '../../layouts/setting.vue';

export default [
    {
        path: '/',
        redirect: '/home',
    },
    {
        path: '/home',
        component: Layout,
        meta: {
            title: {
                zh_CN: '首页',
            },
            icon: shallowRef(DashboardIcon),
            orderNo: 0,
        },
        children: [
            {
                path: '',
                name: 'dashboard',
                component: HomePage,
                meta: {
                    title: {
                        zh_CN: '首页',
                    },
                },
            },
        ],
    },
    {
        path: '/settings',
        component: BlankLayout,
        meta: {
            title: {
                zh_CN: '设置',
            },
            icon: shallowRef(SettingIcon),
            hidden: true,
        },
        children: [
            {
                path: '',
                name: 'settings',
                component: SettingPage,
                meta: {
                    title: {
                        zh_CN: '设置',
                    },
                },
            },
        ],
    },
];