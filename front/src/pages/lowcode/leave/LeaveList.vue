<template>
  <div class="leave-list-page">
    <t-card title="我的申请" bordered>
      <template #actions>
        <t-button theme="primary" @click="router.push('/leave/apply')">
          <template #icon><t-icon name="add" /></template>
          新建申请
        </t-button>
      </template>

      <t-table
        :data="tableData"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        stripe
        hover
        @page-change="onPageChange"
      >
        <template #leaveType="{ row }">
          <t-tag theme="primary" variant="light">{{ leaveTypeLabel(row.leave_type) }}</t-tag>
        </template>
        <template #status="{ row }">
          <t-tag :theme="statusTheme(row.status)" variant="light">{{ statusLabel(row.status) }}</t-tag>
        </template>
        <template #wfStatus="{ row }">
          <t-tag v-if="row._wfStatus" :theme="wfStatusTheme(row._wfStatus)" variant="outline">
            {{ wfStatusLabel(row._wfStatus) }}
          </t-tag>
          <span v-else>-</span>
        </template>
        <template #operation="{ row }">
          <t-space>
            <t-link theme="primary" @click="viewDetail(row.id)">查看详情</t-link>
            <t-link
              v-if="row._wfInstanceId && row._wfStatus === 'pending'"
              theme="warning"
              @click="handleCancel(row)"
            >撤回</t-link>
          </t-space>
        </template>
      </t-table>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import leaveApi from '@/api/lowcode/leave'

const router = useRouter()
const loading = ref(false)
const tableData = ref<any[]>([])
const pagination = ref({ current: 1, pageSize: 10, total: 0 })

const userInfo = computed(() => {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} }
})

const columns = [
  { colKey: 'id', title: '编号', width: 80 },
  { colKey: 'leave_type', title: '请假类型', cell: 'leaveType', width: 100 },
  { colKey: 'leave_days', title: '天数', width: 70 },
  { colKey: 'start_date', title: '开始日期', width: 120 },
  { colKey: 'end_date', title: '结束日期', width: 120 },
  { colKey: 'status', title: '审批状态', cell: 'status', width: 100 },
  { colKey: 'wfStatus', title: '工作流', cell: 'wfStatus', width: 100 },
  { colKey: 'created_at', title: '提交时间', width: 160 },
  { colKey: 'operation', title: '操作', cell: 'operation', width: 160 },
]

const leaveTypeMap: Record<string, string> = { sick: '病假', personal: '事假', public: '公假' }
const statusMap: Record<string, string> = { pending_counselor: '待辅导员审核', pending_dean: '待系主任审核', pending: '待审批', approved: '审核通过', rejected: '已驳回', cancelled: '已撤回' }

function leaveTypeLabel(v: string) { return leaveTypeMap[v] || v }
function statusLabel(v: string) { return statusMap[v] || v }
function statusTheme(v: string): string {
  if (v === 'approved') return 'success'
  if (v === 'rejected' || v === 'cancelled') return 'danger'
  return 'warning'
}
function wfStatusTheme(v: string): string {
  if (v === 'approved') return 'success'
  if (v === 'rejected') return 'danger'
  return 'warning'
}
function wfStatusLabel(v: string) {
  if (v === 'approved') return '已通过'
  if (v === 'rejected') return '已驳回'
  if (v === 'cancelled') return '已撤回'
  return '进行中'
}

async function loadData() {
  loading.value = true
  try {
    const uid = userInfo.value.id || userInfo.value.uid
    if (!uid) { MessagePlugin.warning('请先登录'); return }
    // 查请假记录
    const res = await leaveApi.queryRecords({
      page: pagination.value.current,
      pageSize: pagination.value.pageSize,
      studentId: uid,
    })
    if (res.data.code === 1) {
      const d = res.data.data
      const records = d.content || d.records || []
      // 查工作流实例
      const wfRes = await leaveApi.getMyApplications(uid, 1, 999)
      const wfMap: Record<number, any> = {}
      if (wfRes.data.code === 1) {
        const wfRecords = wfRes.data.data?.content || wfRes.data.data?.records || []
        wfRecords.forEach((w: any) => { wfMap[w.businessId] = w })
      }
      tableData.value = records.map((r: any) => {
        const wf = wfMap[r.id]
        return { ...r, _wfInstanceId: wf?.id, _wfStatus: wf?.status }
      })
      pagination.value.total = d.totalElements || d.total || 0
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function onPageChange(pageInfo: any) {
  pagination.value.current = pageInfo.current
  pagination.value.pageSize = pageInfo.pageSize
  loadData()
}

function viewDetail(id: number) {
  router.push(`/leave/detail/${id}`)
}

function handleCancel(row: any) {
  const dialog = DialogPlugin.confirm({
    header: '确认撤回',
    body: '确定要撤回该请假申请吗？',
    confirmBtn: { content: '确认撤回', theme: 'warning' },
    onConfirm: async () => {
      try {
        await leaveApi.cancel(row._wfInstanceId, userInfo.value.id || userInfo.value.uid)
        MessagePlugin.success('已撤回')
        loadData()
      } catch (e: any) {
        MessagePlugin.error(e?.response?.data?.msg || e?.message || '撤回失败')
      }
      dialog.destroy()
    },
  })
}

onMounted(loadData)
</script>

<style scoped>
.leave-list-page { max-width: 1100px; margin: 0 auto; }
</style>
