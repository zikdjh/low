<template>
  <div class="approval-list-page">
    <t-card title="待审批列表" bordered>
      <t-table
        :data="tableData"
        :columns="columns"
        :loading="loading"
        row-key="id"
        stripe
        hover
      >
        <template #initiator="{ row }">
          <t-avatar size="small" style="margin-right:8px">{{ row.initiatorName?.charAt(0) }}</t-avatar>
          {{ row.initiatorName }}
        </template>
        <template #status="{ row }">
          <t-tag :theme="statusTheme[row.status]" variant="light">{{ statusLabel[row.status] }}</t-tag>
        </template>
        <template #operation="{ row }">
          <t-button theme="primary" size="small" @click="goApprove(row.id)">去审批</t-button>
        </template>
      </t-table>

      <t-empty v-if="!loading && tableData.length === 0" description="暂无待审批的申请" />
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import leaveApi from '@/api/lowcode/leave'

const router = useRouter()
const loading = ref(false)
const tableData = ref<any[]>([])

const userInfo = computed(() => {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} }
})

const statusTheme: Record<string, string> = { pending: 'warning', approved: 'success', rejected: 'danger' }
const statusLabel: Record<string, string> = { pending: '待审批', approved: '已通过', rejected: '已驳回' }

const columns = [
  { colKey: 'id', title: '流程ID', width: 80 },
  { colKey: 'initiator', title: '申请人', cell: 'initiator', width: 130 },
  { colKey: 'workflowName', title: '流程名称', width: 140 },
  { colKey: 'currentNodeIndex', title: '当前节点', width: 100 },
  { colKey: 'totalNodes', title: '总节点', width: 80 },
  { colKey: 'status', title: '状态', cell: 'status', width: 90 },
  { colKey: 'createdAt', title: '发起时间', width: 170 },
  { colKey: 'operation', title: '操作', cell: 'operation', width: 100 },
]

function getRoleCode(): string {
  const roles = userInfo.value.roles || []
  if (roles.includes('counselor')) return 'counselor'
  if (roles.includes('dept_head')) return 'dept_head'
  if (roles.includes('admin') || roles.includes('root')) return 'counselor'
  return ''
}

async function loadData() {
  const roleCode = getRoleCode()
  if (!roleCode) {
    MessagePlugin.warning('当前用户没有审批角色权限')
    return
  }
  loading.value = true
  try {
    const res = await leaveApi.getPendingApprovals(roleCode)
    if (res.data.code === 1) {
      tableData.value = res.data.data || []
    }
  } catch (e: any) {
    MessagePlugin.error('加载审批列表失败')
  } finally {
    loading.value = false
  }
}

function goApprove(instanceId: number) {
  // 走业务应用菜单：辅导员审核页（系主任也可访问 dean_review）
  router.push(`/run/leave_management/leave_counselor_review?instanceId=${instanceId}`)
}

onMounted(loadData)
</script>

<style scoped>
.approval-list-page { max-width: 1100px; margin: 0 auto; }
</style>
