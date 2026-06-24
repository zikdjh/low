<template>
  <div class="approval-detail-page">
    <t-card title="审批处理" bordered :loading="loading">
      <t-loading :loading="loading" text="加载中...">
        <template v-if="leaveData.id">
          <!-- 请假信息 -->
          <t-descriptions :column="2" bordered>
            <t-descriptions-item label="请假类型">
              <t-tag theme="primary" variant="light">{{ leaveTypeLabel(leaveData.leave_type) }}</t-tag>
            </t-descriptions-item>
            <t-descriptions-item label="请假天数">{{ leaveData.leave_days }} 天</t-descriptions-item>
            <t-descriptions-item label="开始日期">{{ leaveData.start_date }}</t-descriptions-item>
            <t-descriptions-item label="结束日期">{{ leaveData.end_date }}</t-descriptions-item>
            <t-descriptions-item label="学生">{{ leaveData.student_name }}</t-descriptions-item>
            <t-descriptions-item label="当前状态">
              <t-tag :theme="statusTheme(leaveData.status)" variant="light">{{ statusLabel(leaveData.status) }}</t-tag>
            </t-descriptions-item>
            <t-descriptions-item label="请假原因" :span="2">{{ leaveData.reason }}</t-descriptions-item>
          </t-descriptions>

          <!-- 工作流进度 -->
          <t-divider>审批进度</t-divider>
          <t-steps :current="progressCurrent" v-if="workflowTasks.length" style="margin-bottom: 24px">
            <t-step-item
              v-for="task in workflowTasks"
              :key="task.id"
              :title="task.nodeName"
              :content="taskContent(task)"
              :status="taskStatusIcon(task)"
            />
          </t-steps>

          <!-- 审批操作 -->
          <div v-if="canApprove" class="approval-actions">
            <t-divider>审批操作</t-divider>
            <t-form label-width="80px">
              <t-form-item label="审批意见">
                <t-textarea
                  v-model="comment"
                  placeholder="请输入审批意见（可选）"
                  :autosize="{ minRows: 2, maxRows: 4 }"
                />
              </t-form-item>
              <t-form-item>
                <t-space size="large">
                  <t-button
                    theme="success"
                    size="large"
                    :loading="approving"
                    @click="handleApprove"
                  >
                    审批通过
                  </t-button>
                  <t-button
                    theme="danger"
                    size="large"
                    variant="outline"
                    :loading="rejecting"
                    @click="handleReject"
                  >
                    驳回
                  </t-button>
                </t-space>
              </t-form-item>
            </t-form>
          </div>
          <t-alert
            v-else-if="wfInstance"
            :theme="wfInstance.status === 'approved' ? 'success' : 'error'"
            :message="wfInstance.status === 'approved' ? '该申请已审批通过' : '该申请已被驳回'"
          />

          <t-space style="margin-top: 16px">
            <t-button theme="default" @click="router.back()">返回</t-button>
          </t-space>
        </template>
        <t-empty v-else description="找不到该审批记录" />
      </t-loading>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import leaveApi from '@/api/lowcode/leave'
import type { WorkflowInstance, WorkflowTask } from '@/types/lowcode'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const approving = ref(false)
const rejecting = ref(false)
const leaveData = ref<any>({})
const wfInstance = ref<WorkflowInstance | null>(null)
const workflowTasks = ref<WorkflowTask[]>([])
const comment = ref('')

const userInfo = computed(() => {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} }
})

const canApprove = computed(() => {
  return wfInstance.value && wfInstance.value.status === 'pending'
})

const progressCurrent = computed(() => {
  if (!wfInstance.value) return 0
  if (wfInstance.value.status === 'approved') return workflowTasks.value.length
  if (wfInstance.value.status === 'rejected') {
    return workflowTasks.value.findIndex(t => t.status === 'rejected') + 1
  }
  return wfInstance.value.currentNodeIndex
})

const leaveTypeMap: Record<string, string> = { sick: '病假', personal: '事假', public: '公假' }
const statusMap: Record<string, string> = { pending: '待审批', approved: '已通过', rejected: '已驳回' }
function leaveTypeLabel(v: string) { return leaveTypeMap[v] || v }
function statusLabel(v: string) { return statusMap[v] || v }
function statusTheme(v: string): string {
  if (v === 'approved') return 'success'
  if (v === 'rejected') return 'danger'
  return 'warning'
}
function taskStatusIcon(task: WorkflowTask): 'default' | 'finish' | 'error' {
  if (task.status === 'approved') return 'finish'
  if (task.status === 'rejected') return 'error'
  return 'default'
}
function taskContent(task: WorkflowTask): string {
  if (task.status === 'approved') return `${task.assigneeName || '-'} 已通过`
  if (task.status === 'rejected') return `${task.assigneeName || '-'} 已驳回`
  return '待审批'
}

async function loadDetail() {
  loading.value = true
  try {
    const instanceId = Number(route.params.instanceId)
    const progressRes = await leaveApi.getWorkflowProgress(instanceId)
    if (progressRes.data.code === 1) {
      const progress = progressRes.data.data
      wfInstance.value = progress.instance
      workflowTasks.value = progress.tasks || []
      if (wfInstance.value) {
        const leaveRes = await leaveApi.getDetail(wfInstance.value.businessId)
        if (leaveRes.data.code === 1) {
          leaveData.value = leaveRes.data.data.leaveData || {}
        }
      }
    }
  } catch (e: any) {
    MessagePlugin.error('加载审批详情失败')
  } finally {
    loading.value = false
  }
}

async function handleApprove() {
  if (!wfInstance.value) return
  approving.value = true
  try {
    const res = await leaveApi.approve({
      instanceId: wfInstance.value.id,
      userId: userInfo.value.id || userInfo.value.uid,
      username: userInfo.value.nickname || userInfo.value.username,
      comment: comment.value,
    })
    if (res.data.code === 1) {
      MessagePlugin.success('审批通过')
      router.push('/run/leave_management/leave_counselor_review')
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '审批失败')
  } finally {
    approving.value = false
  }
}

async function handleReject() {
  if (!wfInstance.value) return
  if (!comment.value.trim()) {
    MessagePlugin.warning('驳回时请填写审批意见')
    return
  }
  rejecting.value = true
  try {
    const res = await leaveApi.reject({
      instanceId: wfInstance.value.id,
      userId: userInfo.value.id || userInfo.value.uid,
      username: userInfo.value.nickname || userInfo.value.username,
      comment: comment.value,
    })
    if (res.data.code === 1) {
      MessagePlugin.success('已驳回')
      router.push('/run/leave_management/leave_counselor_review')
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || '驳回失败')
  } finally {
    rejecting.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.approval-detail-page { max-width: 800px; margin: 0 auto; }
.approval-actions { margin-top: 16px; }
</style>
