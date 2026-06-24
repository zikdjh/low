<template>
  <div class="leave-detail-page">
    <t-card title="申请详情" bordered>
      <!-- 请假信息 -->
      <t-descriptions :column="2" bordered>
        <t-descriptions-item label="请假类型">
          <t-tag theme="primary" variant="light">{{ leaveTypeLabel(leaveData.leave_type) }}</t-tag>
        </t-descriptions-item>
        <t-descriptions-item label="请假天数">{{ leaveData.leave_days }} 天</t-descriptions-item>
        <t-descriptions-item label="开始日期">{{ leaveData.start_date }}</t-descriptions-item>
        <t-descriptions-item label="结束日期">{{ leaveData.end_date }}</t-descriptions-item>
        <t-descriptions-item label="审批状态">
          <t-tag :theme="statusTheme(leaveData.status)" variant="light">{{ statusLabel(leaveData.status) }}</t-tag>
        </t-descriptions-item>
        <t-descriptions-item label="学生">{{ leaveData.student_name }}</t-descriptions-item>
        <t-descriptions-item label="请假原因" :span="2">{{ leaveData.reason }}</t-descriptions-item>
      </t-descriptions>

      <!-- 工作流进度 -->
      <t-divider>审批进度</t-divider>
      <t-steps :current="computedCurrent" layout="vertical" v-if="workflowTasks.length">
        <t-step-item
          v-for="(task, idx) in workflowTasks"
          :key="task.id"
          :title="task.nodeName"
          :content="taskContent(task)"
          :status="taskStatus(task)"
        />
      </t-steps>
      <t-empty v-else description="暂无审批进度" />

      <t-space style="margin-top: 16px">
        <t-button theme="default" @click="router.back()">返回</t-button>
        <t-button
          v-if="wfInstance && wfInstance.status === 'pending' && isInitiator"
          theme="warning"
          variant="outline"
          @click="handleCancel"
        >撤回申请</t-button>
      </t-space>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import leaveApi from '@/api/lowcode/leave'
import type { WorkflowInstance, WorkflowTask } from '@/types/lowcode'

const route = useRoute()
const router = useRouter()
const leaveData = ref<any>({})
const wfInstance = ref<WorkflowInstance | null>(null)
const workflowTasks = ref<WorkflowTask[]>([])
const loading = ref(false)

const userInfo = computed(() => {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} }
})

const isInitiator = computed(() => {
  const uid = userInfo.value.id || userInfo.value.uid
  return wfInstance.value && wfInstance.value.initiatorId === uid
})

const leaveTypeMap: Record<string, string> = { sick: '病假', personal: '事假', public: '公假' }
const statusMap: Record<string, string> = { pending_counselor: '待辅导员审核', pending_dean: '待系主任审核', pending: '待审批', approved: '审核通过', rejected: '已驳回', cancelled: '已撤回' }
function leaveTypeLabel(v: string) { return leaveTypeMap[v] || v }
function statusLabel(v: string) { return statusMap[v] || v }
function statusTheme(v: string): string {
  if (v === 'approved') return 'success'
  if (v === 'rejected' || v === 'cancelled') return 'danger'
  return 'warning'
}

const computedCurrent = computed(() => {
  if (!wfInstance.value) return 0
  if (wfInstance.value.status === 'approved') return workflowTasks.value.length
  if (wfInstance.value.status === 'rejected') {
    return workflowTasks.value.findIndex(t => t.status === 'rejected') + 1
  }
  return wfInstance.value.currentNodeIndex
})

function taskStatus(task: WorkflowTask): 'default' | 'finish' | 'error' {
  if (task.status === 'approved') return 'finish'
  if (task.status === 'rejected') return 'error'
  return 'default'
}

function taskContent(task: WorkflowTask): string {
  if (task.status === 'approved') {
    return `审批人: ${task.assigneeName || '-'} — 审批通过 — ${task.comment || ''}`
  }
  if (task.status === 'rejected') {
    return `审批人: ${task.assigneeName || '-'} — 已驳回 — ${task.comment || '驳回'}`
  }
  return `待 ${task.assigneeRole === 'counselor' ? '辅导员' : '系主任'} 审批`
}

async function loadDetail() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res = await leaveApi.getDetail(id)
    if (res.data.code === 1) {
      leaveData.value = res.data.data.leaveData || {}
      const wf = res.data.data.workflow
      if (wf) {
        wfInstance.value = wf.instance
        workflowTasks.value = wf.tasks || []
      }
    }
  } catch (e: any) {
    MessagePlugin.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  const dialog = DialogPlugin.confirm({
    header: '确认撤回',
    body: '确定要撤回该请假申请吗？',
    confirmBtn: { content: '确认撤回', theme: 'warning' },
    onConfirm: async () => {
      try {
        await leaveApi.cancel(wfInstance.value!.id, userInfo.value.id || userInfo.value.uid)
        MessagePlugin.success('已撤回')
        loadDetail()
      } catch (e: any) {
        MessagePlugin.error(e?.response?.data?.msg || '撤回失败')
      }
      dialog.destroy()
    },
  })
}

onMounted(loadDetail)
</script>

<style scoped>
.leave-detail-page { max-width: 800px; margin: 0 auto; }
</style>
