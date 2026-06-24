<#--
  列表页模板 —— 由低代码平台 release 快照生成
  期望根上下文：page (含 pageCode/name/entityCode/layout)，entity (匹配 entityCode)
-->
<template>
  <div class="${page.pageCode}-list">
    <div class="page-header">
      <h2>${page.name!page.pageCode}</h2>
      <t-button theme="primary" @click="onCreate">新增</t-button>
    </div>

    <t-table
      row-key="${entity.primaryKeyField.camelName!'id'}"
      :data="list"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      @page-change="onPageChange"
    >
      <template #operation="{ row }">
        <t-link theme="primary" @click="onEdit(row)">编辑</t-link>
        <t-link theme="danger" @click="onDelete(row)">删除</t-link>
      </template>
    </t-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ${entity.varName}Api, type ${entity.className} } from '../../api/${appCode}/${entity.code}';

const list = ref<${entity.className}[]>([]);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const columns = [
<#list entity.fields as f>
  <#if f.showInList>
  { colKey: '${f.camelName}', title: '${f.name!f.code}' },
  </#if>
</#list>
  { colKey: 'operation', title: '操作', width: 160, fixed: 'right' },
];

async function loadData() {
  loading.value = true;
  try {
    const res = await ${entity.varName}Api.page({
      page: pagination.value.current - 1,
      size: pagination.value.pageSize,
    });
    const data: any = (res.data as any)?.data ?? res.data ?? {};
    list.value = data.content ?? data ?? [];
    pagination.value.total = data.totalElements ?? list.value.length;
  } finally {
    loading.value = false;
  }
}

function onPageChange(p: any) {
  pagination.value.current = p.current;
  pagination.value.pageSize = p.pageSize;
  loadData();
}

function onCreate() { /* TODO 弹出表单 */ }
function onEdit(_row: ${entity.className}) { /* TODO */ }
async function onDelete(row: ${entity.className}) {
  if (!confirm('确定删除？')) return;
  await ${entity.varName}Api.remove(row.${entity.primaryKeyField.camelName!'id'} as any);
  loadData();
}

onMounted(loadData);
</script>
