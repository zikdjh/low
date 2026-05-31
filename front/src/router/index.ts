import {createRouter, createWebHistory} from "vue-router";
import type {RouteRecordRaw} from "vue-router";

const homepageModules = import.meta.glob("./modules/**/home.ts", {
    eager: true,
});

// 添加登录模块
const loginModules = import.meta.glob("./modules/**/Login.ts", {
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

export default router;