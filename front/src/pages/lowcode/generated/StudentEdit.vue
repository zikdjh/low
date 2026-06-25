<template>
  <div class="student-edit-page">
    <t-card :bordered="false">
      <template #title>{{ isNew ? '新增' : '编辑' }}学生信息</template>

      <t-form
        ref="formRef"
        :data="formData"
        :rules="rules"
        label-width="120px"
        @submit="handleSubmit"
      >
        <t-form-item label="学号" name="studentNo">
          <t-input v-model="formData.studentNo" placeholder="请输入学号" :maxlength="20" />
        </t-form-item>
        <t-form-item label="姓名" name="name">
          <t-input v-model="formData.name" placeholder="请输入姓名" :maxlength="64" />
        </t-form-item>
        <t-form-item label="性别" name="gender">
          <t-input v-model="formData.gender" placeholder="请输入性别" :maxlength="8" />
        </t-form-item>
        <t-form-item label="班级" name="className">
          <t-input v-model="formData.className" placeholder="请输入班级" :maxlength="64" />
        </t-form-item>
        <t-form-item label="年级" name="grade">
          <t-input v-model="formData.grade" placeholder="请输入年级" :maxlength="16" />
        </t-form-item>
        <t-form-item label="院系" name="department">
          <t-input v-model="formData.department" placeholder="请输入院系" :maxlength="64" />
        </t-form-item>
        <t-form-item label="联系电话" name="phone">
          <t-input v-model="formData.phone" placeholder="请输入联系电话" :maxlength="20" />
        </t-form-item>
        <t-form-item label="邮箱" name="email">
          <t-input v-model="formData.email" placeholder="请输入邮箱" :maxlength="128" />
        </t-form-item>
        <t-form-item label="学籍状态" name="status">
          <t-input v-model="formData.status" placeholder="请输入学籍状态" :maxlength="16" />
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
import studentApi from '@/api/lowcode/generated/student';

const route = useRoute();
const router = useRouter();
const formRef = ref<FormInstanceFunctions>();
const submitting = ref(false);

const isNew = computed(() => route.params.id === 'new');
const recordId = computed(() => (isNew.value ? null : Number(route.params.id)));

const formData = reactive<Record<string, any>>({});

const rules: Record<string, FormRule[]> = {
  studentNo: [{ required: true, message: '学号不能为空' }],
  name: [{ required: true, message: '姓名不能为空' }],
  status: [{ required: true, message: '学籍状态不能为空' }],
};

async function loadData() {
  if (isNew.value) return;
  const res: any = await studentApi.getById(recordId.value as number);
  if (res.data) Object.assign(formData, res.data);
}

async function handleSubmit(ctx: SubmitContext) {
  if (ctx.validateResult !== true) return;
  submitting.value = true;
  try {
    if (isNew.value) {
      await studentApi.create(formData);
      MessagePlugin.success('新增成功');
    } else {
      await studentApi.update(recordId.value as number, formData);
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
.student-edit-page { padding: 16px; max-width: 800px; }
</style>
