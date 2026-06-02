<template>
  <div class="table-element">
    <t-table
      :data="mockData"
      :columns="columns"
      :border="element.props.border"
    >
      <template #id="{ row }">
        {{ element.props.showIndex ? row.index : row.id }}
      </template>
    </t-table>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  element: any;
}>();

const columns = computed(() => [
  { 
    colKey: 'id', 
    title: props.element.props.showIndex ? '序号' : 'ID', 
    width: 80 
  },
  { colKey: 'name', title: '名称', width: 150 },
  { colKey: 'code', title: '编码', width: 150 },
  { colKey: 'status', title: '状态', width: 100 },
]);

const mockData = [
  { id: 1, name: '数据项1', code: 'item001', status: '正常', index: 1 },
  { id: 2, name: '数据项2', code: 'item002', status: '正常', index: 2 },
  { id: 3, name: '数据项3', code: 'item003', status: '禁用', index: 3 },
];
</script>

<style scoped lang="less">
.table-element {
  padding: 8px 0;
}
</style>
