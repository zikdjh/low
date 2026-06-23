import request from '@/api/index'
import type {
    LeaveApplication,
    LeaveQueryParams,
    WorkflowInstance,
    WorkflowProgress,
    ApprovalRequest,
    PageResult
} from '@/types/lowcode'

/** 请假管理 API */
const leaveApi = {
    /** 获取数据源（请假类型、状态等） */
    getDataSources() {
        return request.get<{ code: number; data: any }>('/lowcode/leave/datasources')
    },

    /** 提交请假申请 */
    apply(data: LeaveApplication) {
        return request.post<{ code: number; data: any }>('/lowcode/leave/apply', data)
    },

    /** 查询请假记录 */
    queryRecords(params: LeaveQueryParams) {
        return request.get<{ code: number; data: PageResult }>('/lowcode/leave/records', { params })
    },

    /** 获取请假详情（含工作流进度） */
    getDetail(id: number) {
        return request.get<{ code: number; data: any }>('/lowcode/leave/detail/' + id)
    },

    /** 获取待审批列表 */
    getPendingApprovals(roleCode: string) {
        return request.get<{ code: number; data: WorkflowInstance[] }>('/lowcode/leave/pending-approvals', {
            params: { roleCode }
        })
    },

    /** 获取工作流进度 */
    getWorkflowProgress(instanceId: number) {
        return request.get<{ code: number; data: WorkflowProgress }>('/lowcode/leave/workflow-progress/' + instanceId)
    },

    /** 审批通过 */
    approve(data: ApprovalRequest) {
        return request.post<{ code: number; data: any }>('/lowcode/leave/approve', data)
    },

    /** 审批驳回 */
    reject(data: ApprovalRequest) {
        return request.post<{ code: number; data: any }>('/lowcode/leave/reject', data)
    },

    /** 撤回申请 */
    cancel(instanceId: number, userId: number) {
        return request.post<{ code: number; data: any }>('/lowcode/leave/cancel', { instanceId, userId })
    },

    /** 我的申请列表 */
    getMyApplications(userId: number, page: number, pageSize: number) {
        return request.get<{ code: number; data: any }>('/lowcode/leave/my-applications', {
            params: { userId, page, pageSize }
        })
    },

    /** 获取审批节点定义 */
    getApprovalNodes() {
        return request.get<{ code: number; data: any[] }>('/lowcode/leave/approval-nodes')
    }
}

export default leaveApi
