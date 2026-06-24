<#--
  详情页模板 —— 由低代码平台 release 快照生成
-->
<template>
  <div class="${page.pageCode}-detail">
    <div class="page-header">
      <h2>${page.name!page.pageCode}</h2>
      <t-button variant="outline" @click="router.back()">返回</t-button>
    </div>

    <t-descriptions :column="2" :data="descData" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ${entity.varName}Api, type ${entity.className} } from '../../api/${appCode}/${entity.code}';

const route = useRoute();
const router = useRouter();
const detail = ref<${entity.className} | null>(null);

const descData = computed(() => {
  const d = detail.value as any;
  if (!d) return [];
  return [
<#list entity.fields as f>
    { label: '${f.name!f.code}', value: d.${f.camelName} },
</#list>
  ];
});

async function loadData() {
  const id = Number(route.query.id);
  if (!id) return;
  const res = await ${entity.varName}Api.get(id);
  detail.value = (res.data as any)?.data ?? res.data;
}

onMounted(loadData);
</script>
