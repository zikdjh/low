<#--
  表单页模板 —— 由低代码平台 release 快照生成
-->
<template>
  <div class="${page.pageCode}-form">
    <div class="page-header">
      <h2>${page.name!page.pageCode}</h2>
    </div>

    <t-form ref="formRef" :data="form" :rules="rules" label-align="top" @submit="onSubmit">
<#list entity.fields as f>
  <#if f.showInForm && !f.isPrimaryKey>
      <t-form-item label="${f.name!f.code}" name="${f.camelName}">
    <#if f.fieldType == "TEXT" || f.fieldType == "JSON">
        <t-textarea v-model="form.${f.camelName}" placeholder="请输入${f.name!f.code}" />
    <#elseif f.fieldType == "DATE">
        <t-date-picker v-model="form.${f.camelName}" mode="date" />
    <#elseif f.fieldType == "DATETIME">
        <t-date-picker v-model="form.${f.camelName}" mode="date" enable-time-picker />
    <#elseif f.fieldType == "BOOLEAN">
        <t-switch v-model="form.${f.camelName}" />
    <#elseif f.numeric>
        <t-input-number v-model="form.${f.camelName}" />
    <#elseif f.dictCode??>
        <t-select v-model="form.${f.camelName}" :options="dict_${f.dictCode}" placeholder="请选择${f.name!f.code}" />
    <#else>
        <t-input v-model="form.${f.camelName}" placeholder="请输入${f.name!f.code}" />
    </#if>
      </t-form-item>
  </#if>
</#list>

      <t-form-item>
        <t-space>
          <t-button theme="primary" type="submit">保存</t-button>
          <t-button theme="default" @click="onReset">重置</t-button>
        </t-space>
      </t-form-item>
    </t-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import { ${entity.varName}Api, type ${entity.className} } from '../../api/${appCode}/${entity.code}';

const route = useRoute();
const router = useRouter();
const formRef = ref();
const id = route.query.id ? Number(route.query.id) : null;

const form = reactive<${entity.className}>({
<#list entity.fields as f>
  <#if !f.isPrimaryKey>
  ${f.camelName}: ${f.tsDefault},
  </#if>
</#list>
} as ${entity.className});

const rules: any = {
<#list entity.fields as f>
  <#if f.showInForm && !f.nullable && !f.isPrimaryKey>
  ${f.camelName}: [{ required: true, message: '${f.name!f.code}不能为空' }],
  </#if>
</#list>
};

<#list entity.fields as f>
  <#if f.dictCode??>
const dict_${f.dictCode} = ref<{ label: string; value: any }[]>([]);
  </#if>
</#list>

async function loadData() {
  if (id) {
    const res = await ${entity.varName}Api.get(id);
    Object.assign(form, (res.data as any)?.data ?? res.data);
  }
}

async function onSubmit({ validateResult }: any) {
  if (validateResult !== true) return;
  if (id) {
    await ${entity.varName}Api.update(id, form);
  } else {
    await ${entity.varName}Api.create(form);
  }
  MessagePlugin.success('保存成功');
  router.back();
}

function onReset() {
  formRef.value?.reset();
}

onMounted(loadData);
</script>
