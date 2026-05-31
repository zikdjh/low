import { UserIcon } from 'tdesign-icons-vue-next';
import { shallowRef } from 'vue';

import LoginPage from '../../pages/login/index.vue';

export default [
    {
        path: '/login',
        name: 'login',
        component: LoginPage,
        meta: {
            title: {
                zh_CN: '登录/注册',
            },
            icon: shallowRef(UserIcon),
            hidden: true, // 在导航菜单中隐藏
        },
    },
];
