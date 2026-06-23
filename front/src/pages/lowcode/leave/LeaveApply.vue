<template>
  <div class="leave-apply-page">
    <t-card title="请假申请" bordered>
      <t-form ref="formRef" :data="formData" :rules="rules" label-width="100px" @submit="handleSubmit">
        <t-form-item label="请假类型" name="leaveType">
          <t-select v-model="formData.leaveType" placeholder="请选择请假类型" clearable>
            <t-option v-for="item in leaveTypes" :key="item.value" :value="item.value" :label="item.label" />
          </t-select>
        </t-form-item>

        <t-form-item label="开始日期" name="startDate">
          <t-date-picker v-model="formData.startDate" placeholder="请选择开始日期" style="width: 100%" />
        </t-form-item>

        <t-form-item label="结束日期" name="endDate">
          <t-date-picker v-model="formData.endDate" placeholder="请选择结束日期" style="width: 100%" />
        </t-form-item>

        <t-form-item label="请假天数">
          <t-tag theme="primary" variant="light">{{ leaveDays }} 天</t-tag>
        </t-form-item>

        <t-form-item label="请假原因" name="reason">
          <t-textarea v-model="formData.reason" placeholder="请详细说明请假原因..." :maxlength="500" :autosize="{ minRows: 3, maxRows: 6 }" />
        </t-form-item>

        <t-form-item>
          <t-space>
            <t-button theme="primary" type="submit" :loading="submitting">提交申请</t-button>
            <t-button theme="default" variant="outline" @click="handleReset">重置</t-button>
          </t-space>
        </t-form-item>
      </t-form>
    </t-card>

    <!-- 提交成功提示 -->
    <t-dialog v-model:visible="successVisible" header="申请成功" :footer="false" width="480px">
      <div class="success-content">
        <t-icon name="check-circle-filled" size="48px" style="color: var(--td-success-color)" />
        <p class="success-text">请假申请已提交，请等待审批</p>
        <t-space>
          <t-button theme="primary" @click="goToList">查看我的申请</t-button>
          <t-button theme="default" @click="successVisible = false">继续申请</t-button>
        </t-space>
      </div>
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import leaveApi from '@/api/lowcode/leave'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const successVisible = ref(false)
const leaveTypes = ref<{ value: string; label: string }[]>([])

// 获取当前用户
const userInfo = computed(() => {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} }
})

const formData = ref({
  leaveType: '',
  startDate: '',
  endDate: '',
  reason: '',
})

const rules = {
  leaveType: [{ required: true, message: '请选择请假类型' }],
  startDate: [{ required: true, message: '请选择开始日期' }],
  endDate: [{ required: true, message: '请选择结束日期' }],
  reason: [{ required: true, message: '请填写请假原因' }],
}

const leaveDays = computed(() => {
  if (!formData.value.startDate || !formData.value.endDate) return 0
  const s = new Date(formData.value.startDate)
  const e = new Date(formData.value.endDate)
  if (e < s) return 0
  return Math.ceil((e.getTime() - s.getTime()) / (1000 * 60 * 60 * 24)) + 1
})

onMounted(async () => {
  try {
    const res = await leaveApi.getDataSources()
    if (res.data.code === 1 && res.data.data?.leaveTypes) {
      leaveTypes.value = res.data.data.leaveTypes
    }
  } catch { /* ignore */ }
})

async function handleSubmit() {
  const valid = await formRef.value?.validate()
  if (valid !== true) return

  submitting.value = true
  try {
    const res = await leaveApi.apply({
      studentId: userInfo.value.id || userInfo.value.uid || 1,
      studentName: userInfo.value.nickname || userInfo.value.username || '学生',
      leaveType: formData.value.leaveType,
      reason: formData.value.reason,
      startDate: formData.value.startDate,
      endDate: formData.value.endDate,
    })
    if (res.data.code === 1) {
      successVisible.value = true
      handleReset()
    } else {
      MessagePlugin.error(res.data.msg || '提交失败')
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

function handleReset() {
  formData.value = { leaveType: '', startDate: '', endDate: '', reason: '' }
}

function goToList() {
  successVisible.value = false
  router.push('/leave/my-list')
}
</script>

<style scoped>
.leave-apply-page { max-width: 680px; margin: 0 auto; }
.success-content { text-align: center; padding: 20px 0; display: flex; flex-direction: column; align-items: center; gap: 16px; }
.success-text { font-size: 16px; font-weight: 500; color: var(--td-text-color-primary); }
</style>
