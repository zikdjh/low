import {createRouter, createWebHistory} from "vue-router";
import type {RouteRecordRaw} from "vue-router";

const homepageModules = import.meta.glob("./modules/**/home.ts", {
    eager: true,
});

// 添加登录模块
const loginModules = import.meta.glob("./modules/**/login.ts", {
    eager: true,
});

// 添加低代码模块
const lowcodeModules = import.meta.glob("./modules/**/lowcode.ts", {
    eager: true,
});


export const homepageRouterList: Array<RouteRecordRaw> =
    mapModuleRouterList(homepageModules);

// 添加登录路由列表
export const loginRouterList: Array<RouteRecordRaw> =
    mapModuleRouterList(loginModules);

// 添加低代码路由列表
export const lowcodeRouterList: Array<RouteRecordRaw> =
    mapModuleRouterList(lowcodeModules);

// 将登录路由添加到所有路由中
export const allRoutes = [...homepageRouterList, ...loginRouterList, ...lowcodeRouterList];

// 固定路由模块转换为路由
export function mapModuleRouterList(
    modules: Record<string, unknown>,
): Array<RouteRecordRaw> {
    const routerList: Array<RouteRecordRaw> = [];
    Object.keys(modules).forEach((key) => {
        // @ts-expect-error 外部赋值不太好直接写类型
        const mod = modules[key].default || {};
        const modList = Array.isArray(mod) ? [...mod] : [mod];
        routerList.push(...modList);
    });
    return routerList;
}

const router = createRouter({
    history: createWebHistory(),
    routes: allRoutes
});

// 路由守卫
router.beforeEach((to) => {
    // 设置页面标题
    if (to.meta.title) {
        document.title = (to.meta.title as any).zh_CN || '低代码开发平台';
    }
    
    // 检查是否需要登录（当前后端无 token，使用 userInfo 判定登录态）
    const userInfo = localStorage.getItem('userInfo');
    const token = localStorage.getItem('token');
    const access = sessionStorage.getItem('access');
    const isLoggedIn = userInfo || token || access;
    const publicPages = ['/login'];
    
    if (!isLoggedIn && !publicPages.includes(to.path)) {
        // 未登录且访问需要权限的页面，跳转到登录页
        return {
            path: '/login',
            query: { redirect: to.fullPath }
        };
    } else if (isLoggedIn && to.path === '/login') {
        // 已登录访问登录页，跳转到首页
        return '/home';
    }
});

export default router;