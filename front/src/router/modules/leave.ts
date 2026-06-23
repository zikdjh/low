import { shallowRef } from 'vue'
import { CalendarIcon } from 'tdesign-icons-vue-next'
import Layout from '../../layouts/index.vue'

/**
 * 请假管理路由 —— 使用低代码平台 PageViewer 渲染
 * 
 * 所有页面均由 LeavePageInitializer 自动生成 PageSchema 记录，
 * 前端通过 PageViewer 组件根据 pageCode 动态加载 layoutJson 并渲染。
 * 不再手写 Vue 页面文件。
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
    ],
  },
]
