<#--
  应用菜单常量 —— 由低代码平台 release 快照生成
  期望根上下文：appCode，menu (MenuNode 树)
-->
export interface AppMenuItem {
  name: string;
  icon?: string;
  pageCode?: string;
  routePath?: string;
  children?: AppMenuItem[];
}

<#-- 递归宏：渲染 MenuNode 树 -->
<#macro renderNode node indent>
  <#assign pad = ""?left_pad(indent * 2) />
${pad}{
${pad}  name: "${node.name?j_string}",
<#if node.icon?? && node.icon?length gt 0>
${pad}  icon: "${node.icon?j_string}",
</#if>
<#if node.pageCode?? && node.pageCode?length gt 0>
${pad}  pageCode: "${node.pageCode}",
</#if>
<#if node.routePath?? && node.routePath?length gt 0>
${pad}  routePath: "${node.routePath}",
</#if>
<#if node.children?? && node.children?size gt 0>
${pad}  children: [
  <#list node.children as ch>
    <@renderNode node=ch indent=indent+2 />
    <#sep>,
  </#list>
${pad}  ],
</#if>
${pad}}<#nt>
</#macro>

export const ${appCode}Menu: AppMenuItem[] = [
<#list menu as node>
  <@renderNode node=node indent=1 /><#sep>,
</#list>
];
