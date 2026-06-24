/**
 * 业务应用运行时路由 —— 独立布局，不使用 Layout 包裹
 * AppRuntime 自己提供完整的侧边栏导航 + 页面渲染
 *
 * 路径形式：
 *   /run/:appCode                  —— 仅指定应用，进入欢迎页
 *   /run/:appCode/:pageCode        —— 深链直达某个页面，刷新可保留
 *
 * 同一份组件 component 复用，pageCode 可选；AppRuntime 内部 watch route.params
 * 在切换菜单时 router.replace，实现真正的"地址栏跟随当前页面"。
 */
export default [
  {
    path: '/run/:appCode/:pageCode?',
    name: 'AppRuntime',
    component: () => import('../../pages/lowcode/app/AppRuntime.vue'),
    meta: {
      title: { zh_CN: '业务应用' },
      hidden: true,
    },
  },
];
