<#--
  应用路由模块 —— 由低代码平台 release 快照生成
  期望根上下文：appCode，pages（每项需 pageCode、name、componentName）
-->
import { AppIcon } from 'tdesign-icons-vue-next';
import { shallowRef } from 'vue';
import Layout from '../../layouts/index.vue';

export default [
  {
    path: '/${appCode}',
    component: Layout,
    name: '${appCode}',
    meta: {
      title: { zh_CN: '${appName!appCode}' },
      icon: shallowRef(AppIcon),
    },
    children: [
<#list pages as p>
      {
        path: '${p.pageCode}',
        name: '${appCode}_${p.pageCode}',
        component: () => import('../../views/${appCode}/${p.componentName}.vue'),
        meta: { title: { zh_CN: '${p.name!p.pageCode}' } },
      },
</#list>
    ],
  },
];
