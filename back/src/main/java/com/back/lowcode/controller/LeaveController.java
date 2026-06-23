package com.back.lowcode.controller;

import com.back.common.Result;
import com.back.lowcode.entity.WorkflowInstance;
import com.back.lowcode.service.LeaveService;
import com.back.lowcode.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 请假管理 & 工作流审批控制器
 * 提供三个核心服务：
 * 1. 数据查询服务：请假记录查询、支持分页和条件筛选
 * 2. 数据源服务：请假类型、状态等下拉数据源
 * 3. 数据库操作服务：申请提交、审批处理等增删改查
 */
@Slf4j
@RestController
@RequestMapping("/lowcode/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final WorkflowService workflowService;

    // ==================== 数据源服务 ====================

    /**
     * 获取下拉数据源（请假类型、审批节点等）
     */
    @GetMapping("/datasources")
    public Result getDataSources() {
        return Result.success(leaveService.getDataSources());
    }

    // ==================== 申请提交（数据库操作服务） ====================

    /**
     * 提交请假申请 — 发起工作流
     */
    @PostMapping("/apply")
    public Result submitApplication(@RequestBody Map<String, Object> request) {
        Long studentId = Long.valueOf(request.get("studentId").toString());
        String studentName = (String) request.get("studentName");
        String leaveType = (String) request.get("leaveType");
        String reason = (String) request.get("reason");
        LocalDate startDate = LocalDate.parse((String) request.get("startDate"));
        LocalDate endDate = LocalDate.parse((String) request.get("endDate"));

        Map<String, Object> result = leaveService.submitApplication(
                studentId, studentName, leaveType, reason, startDate, endDate);
        return Result.success(result);
    }

    // ==================== 数据查询服务 ====================

    /**
     * 查询请假记录 — 支持表格分页和条件筛选
     */
    @GetMapping("/records")
    public Result queryRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String leaveType) {
        Page<Map<String, Object>> records = leaveService.queryLeaveRecords(
                page, pageSize, studentId, status, leaveType);
        return Result.success(records);
    }

    /**
     * 获取请假详情（含工作流进度）
     */
    @GetMapping("/detail/{id}")
    public Result getDetail(@PathVariable Long id) {
        Map<String, Object> detail = leaveService.getLeaveFullDetail(id);
        return Result.success(detail);
    }

    // ==================== 工作流相关 ====================

    /**
     * 获取当前用户的待审批列表
     */
    @GetMapping("/pending-approvals")
    public Result getPendingApprovals(@RequestParam String roleCode) {
        List<WorkflowInstance> list = workflowService.getPendingApprovals(roleCode);
        return Result.success(list);
    }

    /**
     * 获取工作流进度详情
     */
    @GetMapping("/workflow-progress/{instanceId}")
    public Result getWorkflowProgress(@PathVariable Long instanceId) {
        Map<String, Object> progress = workflowService.getProgress(instanceId);
        return Result.success(progress);
    }

    /**
     * 审批通过
     */
    @PostMapping("/approve")
    public Result approve(@RequestBody Map<String, Object> request) {
        Long instanceId = Long.valueOf(request.get("instanceId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        String username = (String) request.get("username");
        String comment = (String) request.getOrDefault("comment", "");

        Map<String, Object> resultMap = workflowService.approve(instanceId, userId, username, comment);

        // 如果是最终审批通过，更新请假记录状态
        WorkflowInstance wfi = (WorkflowInstance) resultMap.get("instance");
        if ("approved".equals(wfi.getStatus())) {
            leaveService.updateLeaveStatus(wfi.getBusinessId(), "approved");
        }

        return Result.success(resultMap);
    }

    /**
     * 审批驳回
     */
    @PostMapping("/reject")
    public Result reject(@RequestBody Map<String, Object> request) {
        Long instanceId = Long.valueOf(request.get("instanceId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        String username = (String) request.get("username");
        String comment = (String) request.getOrDefault("comment", "驳回");

        Map<String, Object> resultMap = workflowService.reject(instanceId, userId, username, comment);
        WorkflowInstance wfi = (WorkflowInstance) resultMap.get("instance");
        leaveService.updateLeaveStatus(wfi.getBusinessId(), "rejected");

        return Result.success(resultMap);
    }

    /**
     * 撤回申请
     */
    @PostMapping("/cancel")
    public Result cancel(@RequestBody Map<String, Object> request) {
        Long instanceId = Long.valueOf(request.get("instanceId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        WorkflowInstance wfi = workflowService.cancel(instanceId, userId);
        if (wfi.getBusinessId() != null) {
            leaveService.updateLeaveStatus(wfi.getBusinessId(), "cancelled");
        }
        return Result.success(Map.of("message", "已撤回"));
    }

    /**
     * 我的申请列表
     */
    @GetMapping("/my-applications")
    public Result getMyApplications(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<WorkflowInstance> list = workflowService.getMyApplications(userId, page, pageSize);
        return Result.success(list);
    }

    /**
     * 获取审批节点定义
     */
    @GetMapping("/approval-nodes")
    public Result getApprovalNodes() {
        return Result.success(workflowService.getApprovalNodes());
    }
}
