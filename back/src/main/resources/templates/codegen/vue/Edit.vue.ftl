<template>
  <div class="${entity.classNameLower}-edit-page">
    <t-card :bordered="false">
      <template #title>{{ isNew ? '新增' : '编辑' }}${entity.name}</template>

      <t-form
        ref="formRef"
        :data="formData"
        :rules="rules"
        label-width="120px"
        @submit="handleSubmit"
      >
<#list formFields as f>
        <t-form-item label="${f.name}" name="${f.camelName}">
  <#if f.javaType == "Boolean">
          <t-switch v-model="formData.${f.camelName}" />
  <#elseif f.javaType == "LocalDate">
          <t-date-picker v-model="formData.${f.camelName}" mode="date" clearable />
  <#elseif f.javaType == "LocalDateTime">
          <t-date-picker v-model="formData.${f.camelName}" mode="date" enable-time-picker clearable />
  <#elseif f.javaType == "Integer" || f.javaType == "Long">
          <t-input-number v-model="formData.${f.camelName}" />
  <#elseif f.javaType == "Double" || f.javaType == "BigDecimal">
          <t-input-number v-model="formData.${f.camelName}" :decimal-places="${f.scale!2}" />
  <#elseif f.fieldType == "TEXT" || f.fieldType == "JSON">
          <t-textarea v-model="formData.${f.camelName}" :autosize="{ minRows: 3 }" placeholder="请输入${f.name}" />
  <#else>
          <t-input v-model="formData.${f.camelName}" placeholder="请输入${f.name}"<#if f.maxLength??> :maxlength="${f.maxLength}"</#if> />
  </#if>
        </t-form-item>
</#list>

        <t-form-item>
          <t-button theme="primary" type="submit" :loading="submitting">保存</t-button>
          <t-button theme="default" style="margin-left: 12px" @click="handleCancel">取消</t-button>
        </t-form-item>
      </t-form>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import type { FormRule, FormInstanceFunctions, SubmitContext } from 'tdesign-vue-next';
import ${entity.classNameLower}Api from '@/api/${entity.classNameLower}';

const route = useRoute();
const router = useRouter();
const formRef = ref<FormInstanceFunctions>();
const submitting = ref(false);

const isNew = computed(() => route.params.id === 'new');
const recordId = computed(() => (isNew.value ? null : Number(route.params.id)));

const formData = reactive<Record<string, any>>({});

const rules: Record<string, FormRule[]> = {
<#list formFields as f>
  <#if f.required>
  ${f.camelName}: [{ required: true, message: '${f.name}不能为空' }],
  </#if>
</#list>
};

async function loadData() {
  if (isNew.value) return;
  const res: any = await ${entity.classNameLower}Api.getById(recordId.value as number);
  if (res.data) Object.assign(formData, res.data);
}

async function handleSubmit(ctx: SubmitContext) {
  if (ctx.validateResult !== true) return;
  submitting.value = true;
  try {
    if (isNew.value) {
      await ${entity.classNameLower}Api.create(formData);
      MessagePlugin.success('新增成功');
    } else {
      await ${entity.classNameLower}Api.update(recordId.value as number, formData);
      MessagePlugin.success('更新成功');
    }
    router.back();
  } catch (e: any) {
    MessagePlugin.error(e?.message || '保存失败');
  } finally {
    submitting.value = false;
  }
}

function handleCancel() {
  router.back();
}

onMounted(loadData);
</script>

<style scoped>
.${entity.classNameLower}-edit-page { padding: 16px; max-width: 800px; }
</style>
