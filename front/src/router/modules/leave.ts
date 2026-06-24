import { shallowRef } from 'vue'
import { CalendarIcon } from 'tdesign-icons-vue-next'
import Layout from '../../layouts/index.vue'

/**
 * 请假管理路由 —— 使用低代码平台 PageViewer 渲染 + 自定义管理员页面
 * 
 * 大部分页面均由 LeavePageInitializer/LeavePageInitializerV2 自动生成 PageSchema 记录，
 * 前端通过 PageViewer 组件根据 pageCode 动态加载 layoutJson 并渲染。
 * 管理员页面（用户管理、假条管理）使用独立的自定义组件。
 */
export default [
  {
    path: '/leave',
    component: Layout,
    redirect: '/leave/apply',
    name: 'leave',
    meta: {
      title: { zh_CN: '请假管理' },
      icon: shallowRef(CalendarIcon),
      orderNo: 50,
    },
    children: [
      {
        path: 'apply',
        name: 'LeaveApply',
        component: () => import('../../pages/lowcode/page/PageViewer.vue'),
        meta: { title: { zh_CN: '请假申请' }, pageCode: 'leave_application_form' },
      },
      {
        path: 'my-list',
        name: 'LeaveList',
        component: () => import('../../pages/lowcode/page/PageViewer.vue'),
        meta: { title: { zh_CN: '请假记录' }, pageCode: 'leave_application_list' },
      },
      {
        path: 'students',
        name: 'StudentManage',
        component: () => import('../../pages/lowcode/page/PageViewer.vue'),
        meta: { title: { zh_CN: '学生管理' }, pageCode: 'leave_student_list' },
      },
      {
        path: 'approvers',
        name: 'ApproverManage',
        component: () => import('../../pages/lowcode/page/PageViewer.vue'),
        meta: { title: { zh_CN: '教职工管理' }, pageCode: 'leave_approver_list' },
      },
      {
        path: 'detail/:id',
        name: 'LeaveDetail',
        component: () => import('../../pages/lowcode/leave/LeaveDetail.vue'),
        meta: { title: { zh_CN: '申请详情' } },
      },
      // ===== 管理员专用页面 =====
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
