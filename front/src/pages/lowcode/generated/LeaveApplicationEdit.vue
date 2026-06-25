<template>
  <div class="leaveApplication-edit-page">
    <t-card :bordered="false">
      <template #title>{{ isNew ? '新增' : '编辑' }}请假申请</template>

      <t-form
        ref="formRef"
        :data="formData"
        :rules="rules"
        label-width="120px"
        @submit="handleSubmit"
      >
        <t-form-item label="学生" name="studentId">
          <t-input-number v-model="formData.studentId" />
        </t-form-item>
        <t-form-item label="请假类型" name="leaveType">
          <t-input v-model="formData.leaveType" placeholder="请输入请假类型" :maxlength="32" />
        </t-form-item>
        <t-form-item label="请假原因" name="reason">
          <t-textarea v-model="formData.reason" :autosize="{ minRows: 3 }" placeholder="请输入请假原因" />
        </t-form-item>
        <t-form-item label="开始日期" name="startDate">
          <t-date-picker v-model="formData.startDate" mode="date" clearable />
        </t-form-item>
        <t-form-item label="结束日期" name="endDate">
          <t-date-picker v-model="formData.endDate" mode="date" clearable />
        </t-form-item>

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
import leaveApplicationApi from '@/api/lowcode/generated/leaveApplication';

const route = useRoute();
const router = useRouter();
const formRef = ref<FormInstanceFunctions>();
const submitting = ref(false);

const isNew = computed(() => route.params.id === 'new');
const recordId = computed(() => (isNew.value ? null : Number(route.params.id)));

const formData = reactive<Record<string, any>>({});

const rules: Record<string, FormRule[]> = {
  studentId: [{ required: true, message: '学生不能为空' }],
  leaveType: [{ required: true, message: '请假类型不能为空' }],
  reason: [{ required: true, message: '请假原因不能为空' }],
  startDate: [{ required: true, message: '开始日期不能为空' }],
  endDate: [{ required: true, message: '结束日期不能为空' }],
};

async function loadData() {
  if (isNew.value) return;
  const res: any = await leaveApplicationApi.getById(recordId.value as number);
  if (res.data) Object.assign(formData, res.data);
}

async function handleSubmit(ctx: SubmitContext) {
  if (ctx.validateResult !== true) return;
  submitting.value = true;
  try {
    if (isNew.value) {
      await leaveApplicationApi.create(formData);
      MessagePlugin.success('新增成功');
    } else {
      await leaveApplicationApi.update(recordId.value as number, formData);
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
.leaveApplication-edit-page { padding: 16px; max-width: 800px; }
</style>
