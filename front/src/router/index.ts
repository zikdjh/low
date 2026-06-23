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

// 添加请假管理模块
const leaveModules = import.meta.glob("./modules/**/leave.ts", {
    eager: true,
});

// 添加管理员模块
const adminModules = import.meta.glob("./modules/**/admin.ts", {
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

// 添加请假管理路由列表
export const leaveRouterList: Array<RouteRecordRaw> =
    mapModuleRouterList(leaveModules);

// 添加管理员路由列表
export const adminRouterList: Array<RouteRecordRaw> =
    mapModuleRouterList(adminModules);

// 将登录路由添加到所有路由中
export const allRoutes = [...homepageRouterList, ...loginRouterList, ...lowcodeRouterList, ...leaveRouterList, ...adminRouterList];

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
    
    // 登录态：以 sessionStorage.access (JWT access token) 为权威锚点；
    // userInfo 只用于显示用，刷新页签后 access 自动失效，userInfo 也不再被信任。
    const access = sessionStorage.getItem('access');
    const userInfoStr = localStorage.getItem('userInfo');
    const isLoggedIn = !!access && !!userInfoStr;
    const publicPages = ['/login', '/admin/login'];
    const isAdminRoute = to.path.startsWith('/admin');
    
    if (!isLoggedIn && !publicPages.includes(to.path)) {
        // 未登录且访问需要权限的页面，跳转到对应登录页
        if (isAdminRoute) {
            return { path: '/admin/login', query: { redirect: to.fullPath } };
        }
        return {
            path: '/login',
            query: { redirect: to.fullPath }
        };
    }

    if (isLoggedIn) {
        // 判断当前用户角色
        let userRoles: string[] = [];
        try {
            const ui = JSON.parse(userInfoStr || '{}');
            userRoles = ui.roles || [];
        } catch {}

        const isAdmin = userRoles.includes('admin') || userRoles.includes('root');

        // 已登录访问非管理员登录页
        if (to.path === '/login') {
            return '/home';
        }
        if (to.path === '/admin/login') {
            return '/admin/users';
        }

        // 非管理员访问管理后台 → 重定向到用户首页
        if (isAdminRoute && !isAdmin) {
            return '/home';
        }
    }
});

export default router;