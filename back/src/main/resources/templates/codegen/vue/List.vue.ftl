<template>
  <div class="${entity.classNameLower}-list-page">
    <t-card :bordered="false">
      <template #title>${entity.name}列表</template>
      <template #actions>
        <t-button theme="primary" @click="handleCreate">
          <template #icon><plus-icon /></template>
          新增
        </t-button>
      </template>

      <!-- 搜索栏 -->
<#if searchFields?size gt 0>
      <t-form :data="searchForm" layout="inline" @submit="handleSearch" @reset="handleReset" class="search-form">
  <#list searchFields as f>
        <t-form-item label="${f.name}" name="${f.camelName}">
    <#if f.javaType == "Boolean">
          <t-select v-model="searchForm.${f.camelName}" placeholder="请选择" clearable style="width: 160px">
            <t-option :value="true" label="是" />
            <t-option :value="false" label="否" />
          </t-select>
    <#elseif f.javaType == "LocalDate">
          <t-date-picker v-model="searchForm.${f.camelName}" clearable />
    <#elseif f.javaType == "LocalDateTime">
          <t-date-picker v-model="searchForm.${f.camelName}" mode="date" clearable />
    <#else>
          <t-input v-model="searchForm.${f.camelName}" placeholder="请输入${f.name}" clearable style="width: 200px" />
    </#if>
        </t-form-item>
  </#list>
        <t-form-item>
          <t-button theme="primary" type="submit">查询</t-button>
          <t-button theme="default" type="reset" style="margin-left: 8px">重置</t-button>
        </t-form-item>
      </t-form>
</#if>

      <t-table
        :data="tableData"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        bordered
        stripe
        @page-change="onPageChange"
      >
        <template #operation="{ row }">
          <t-link theme="primary" @click="handleEdit(row)">编辑</t-link>
          <t-popconfirm content="确认删除?" @confirm="handleDelete(row)">
            <t-link theme="danger" style="margin-left: 12px">删除</t-link>
          </t-popconfirm>
        </template>
      </t-table>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { PlusIcon } from 'tdesign-icons-vue-next';
import ${entity.classNameLower}Api from '@/api/${entity.classNameLower}';

const router = useRouter();
const loading = ref(false);
const tableData = ref<any[]>([]);
const searchForm = reactive<Record<string, any>>({});
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });

const columns: PrimaryTableCol[] = [
<#list listFields as f>
  { colKey: '${f.camelName}', title: '${f.name}', ellipsis: true },
</#list>
  { colKey: 'operation', title: '操作', width: 160, fixed: 'right' },
];

async function loadData() {
  loading.value = true;
  try {
    const params: any = { pageNum: pagination.current, pageSize: pagination.pageSize };
    Object.keys(searchForm).forEach(k => {
      if (searchForm[k] !== undefined && searchForm[k] !== '' && searchForm[k] !== null) {
        params[k] = searchForm[k];
      }
    });
    const res: any = await ${entity.classNameLower}Api.list(params);
    tableData.value = res.data?.list || [];
    pagination.total = res.data?.total || 0;
  } catch (e: any) {
    MessagePlugin.error(e?.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.current = 1;
  loadData();
}
function handleReset() {
  Object.keys(searchForm).forEach(k => delete searchForm[k]);
  pagination.current = 1;
  loadData();
}
function onPageChange(p: { current: number; pageSize: number }) {
  pagination.current = p.current;
  pagination.pageSize = p.pageSize;
  loadData();
}
function handleCreate() {
  router.push({ name: '${entity.className}Edit', params: { id: 'new' } });
}
function handleEdit(row: any) {
  router.push({ name: '${entity.className}Edit', params: { id: row.id } });
}
async function handleDelete(row: any) {
  await ${entity.classNameLower}Api.delete(row.id);
  MessagePlugin.success('删除成功');
  loadData();
}

onMounted(loadData);
</script>

<style scoped>
.${entity.classNameLower}-list-page { padding: 16px; }
.search-form { margin-bottom: 16px; }
</style>
