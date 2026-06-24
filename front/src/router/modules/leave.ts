import { shallowRef } from 'vue'
import { CalendarIcon } from 'tdesign-icons-vue-next'
import Layout from '../../layouts/index.vue'

/**
 * 请假管理路由 —— Phase 2 后大部分页面由 `/run/leave_management/*` 接管：
 *   - 业务页面（学生/辅导员/系主任/管理员各端）走 SchemaRenderer + 菜单树（lc_app_menu）
 *   - 此处仅保留管理员自定义组件页（独立逻辑，未迁低代码）
 *
 * 想看完整菜单：访问 `/run/leave_management`
 */
export default [
  {
    path: '/leave',
    component: Layout,
    redirect: '/run/leave_management',
    name: 'leave',
    meta: {
      title: { zh_CN: '请假管理' },
      icon: shallowRef(CalendarIcon),
      orderNo: 50,
      hidden: true, // 顶部菜单隐藏；用户通过业务应用入口或 /run/* 进入
    },
    children: [
      // ===== 管理员专用页面（独立组件，未走 PageViewer） =====
      {
        path: 'admin/users',
        name: 'LeaveAdminUsers',
        component: () => import('../../pages/lowcode/leave/LeaveAdminUsers.vue'),
        meta: { title: { zh_CN: '用户管理' }, roles: ['admin'] },
      },
      {
        path: 'admin/leaves',
        name: 'LeaveAdminLeaves',
        component: () => import('../../pages/lowcode/leave/LeaveAdminLeaves.vue'),
        meta: { title: { zh_CN: '假条管理' }, roles: ['admin'] },
      },
    ],
  },
]
