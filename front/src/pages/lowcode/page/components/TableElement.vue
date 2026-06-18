<template>
  <div class="table-element">
    <t-table
      :data="tableData"
      :columns="columns"
      :border="element.props.border"
      :loading="loading"
      :pagination="pagination"
      row-key="id"
      stripe
      @page-change="onPageChange"
    >
      <template v-for="col in columns" :key="col.colKey" #[col.colKey]="{ row }">
        <template v-if="col.type === 'slot'">
          {{ formatFieldValue(row[col.colKey], col.fieldType) }}
        </template>
        <template v-else>
          {{ row[col.colKey] }}
        </template>
      </template>
    </t-table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue';
import dynamicDataApi from '../../../../api/lowcode/dynamicData';

const props = defineProps<{
  element: any;
}>();

const loading = ref(false);
const tableData = ref<any[]>([]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const columns = computed(() => {
  const cols: any[] = [];
  const propsData = props.element.props || {};
  
  if (propsData.showIndex) {
    cols.push({
      colKey: 'index',
      title: '序号',
      width: 80,
    });
  }
  
  if (propsData.columns && Array.isArray(propsData.columns)) {
    propsData.columns.forEach((col: any) => {
      cols.push({
        colKey: col.fieldCode || col.colKey,
        title: col.title || col.name,
        width: col.width || 150,
        type: 'slot',
        fieldType: col.fieldType,
      });
    });
  } else {
    cols.push(
      { colKey: 'id', title: 'ID', width: 80, type: 'slot', fieldType: 'LONG' },
      { colKey: 'name', title: '名称', width: 150, type: 'slot', fieldType: 'VARCHAR' },
      { colKey: 'code', title: '编码', width: 150, type: 'slot', fieldType: 'VARCHAR' },
    );
  }
  
  return cols;
});

function formatFieldValue(value: any, fieldType?: string): string {
  if (value === null || value === undefined) return '-';
  
  switch (fieldType) {
    case 'BOOLEAN':
      return value ? '是' : '否';
    case 'DATE':
    case 'DATETIME':
      return formatDateTime(value);
    case 'DECIMAL':
      return Number(value).toFixed(2);
    default:
      return String(value);
  }
}

function formatDateTime(value: string): string {
  if (!value) return '-';
  const date = new Date(value);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
}

async function fetchData() {
  const entityCode = props.element.props?.entityCode;
  if (!entityCode) {
    tableData.value = getMockData();
    return;
  }
  
  loading.value = true;
  try {
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize,
    };
    const res = await dynamicDataApi.list(entityCode, params);
    if (res.data.code === 1) {
      const data = res.data.data;
      tableData.value = data.content || data.records || [];
      pagination.total = data.totalElements || data.total || 0;
      
      if (props.element.props?.showIndex) {
        tableData.value = tableData.value.map((row: any, index: number) => ({
          ...row,
          index: (pagination.current - 1) * pagination.pageSize + index + 1,
        }));
      }
    }
  } catch (e) {
    console.error('Failed to fetch table data:', e);
    tableData.value = getMockData();
  } finally {
    loading.value = false;
  }
}

function getMockData() {
  return [
    { id: 1, name: '数据项1', code: 'item001', status: '正常', index: 1 },
    { id: 2, name: '数据项2', code: 'item002', status: '正常', index: 2 },
    { id: 3, name: '数据项3', code: 'item003', status: '禁用', index: 3 },
  ];
}

function onPageChange(pageInfo: { current: number; pageSize: number }) {
  pagination.current = pageInfo.current;
  pagination.pageSize = pageInfo.pageSize;
  fetchData();
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="less">
.table-element {
  padding: 8px 0;
}
</style>