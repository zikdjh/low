/**
 * 业务应用运行时路由 —— 独立布局，不使用 Layout 包裹
 * AppRuntime 自己提供完整的侧边栏导航 + 页面渲染
 */
export default [
  {
    path: '/run/:appCode',
    name: 'AppRuntime',
    component: () => import('../../pages/lowcode/app/AppRuntime.vue'),
    meta: {
      title: { zh_CN: '业务应用' },
      hidden: true,
    },
  },
];
