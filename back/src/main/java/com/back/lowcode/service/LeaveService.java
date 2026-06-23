package com.back.lowcode.service;

import com.back.lowcode.entity.WorkflowInstance;
import com.back.lowcode.repository.WorkflowInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 请假管理服务 — 实现学生请假申请、查询、数据统计等核心业务逻辑
 * 体现低代码平台「后端服务可视化配置」的思路
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveService {

    private final DynamicDataService dynamicDataService;
    private final WorkflowService workflowService;
    private final WorkflowInstanceRepository workflowInstanceRepository;

    /** 业务实体编码 */
    public static final String LEAVE_ENTITY_CODE = "leave_application";

    /**
     * 提交请假申请 - 创建业务数据并发起工作流
     */
    @Transactional
    public Map<String, Object> submitApplication(Long studentId, String studentName,
                                                  String leaveType, String reason,
                                                  LocalDate startDate, LocalDate endDate) {
        // 构建请假申请数据
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("student_id", studentId);
        data.put("student_name", studentName);
        data.put("leave_type", leaveType);
        data.put("reason", reason);
        data.put("start_date", startDate.toString());
        data.put("end_date", endDate.toString());
        // 计算请假天数
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        data.put("leave_days", days);
        data.put("status", "pending");

        // 创建业务数据
        Map<String, Object> savedData = dynamicDataService.createData(LEAVE_ENTITY_CODE, data);
        Long businessId = Long.valueOf(savedData.get("id").toString());

        // 发起工作流
        WorkflowInstance wf = workflowService.startWorkflow(
                LEAVE_ENTITY_CODE,
                businessId,
                "leave_approval",
                "学生请假审批",
                studentId,
                studentName
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("leaveData", savedData);
        result.put("workflow", wf);
        result.put("message", "请假申请已提交，请等待审批");
        return result;
    }

    /**
     * 查询请假历史记录 — 支持表格分页和条件筛选
     */
    public Page<Map<String, Object>> queryLeaveRecords(Integer page, Integer pageSize,
                                                       Long studentId, String status, String leaveType) {
        Map<String, String> filters = new HashMap<>();
        if (studentId != null) {
            filters.put("student_id", String.valueOf(studentId));
        }
        if (status != null && !status.isEmpty()) {
            filters.put("status", status);
        }
        if (leaveType != null && !leaveType.isEmpty()) {
            filters.put("leave_type", leaveType);
        }
        return dynamicDataService.listData(LEAVE_ENTITY_CODE, page, pageSize, filters);
    }

    /**
     * 查询单条请假记录
     */
    public Map<String, Object> getLeaveDetail(Long id) {
        return dynamicDataService.getDataById(LEAVE_ENTITY_CODE, id);
    }

    /**
     * 更新请假记录状态（审批完成后回调）
     */
    @Transactional
    public void updateLeaveStatus(Long leaveId, String status) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", status);
        dynamicDataService.updateData(LEAVE_ENTITY_CODE, leaveId, updateData);
    }

    /**
     * 获取请假的完整详情（含工作流进度）
     */
    public Map<String, Object> getLeaveFullDetail(Long leaveId) {
        Map<String, Object> leaveData = dynamicDataService.getDataById(LEAVE_ENTITY_CODE, leaveId);

        // 查找关联的工作流
        List<WorkflowInstance> wfList = workflowInstanceRepository
                .findByBusinessIdAndBusinessCode(leaveId, LEAVE_ENTITY_CODE);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("leaveData", leaveData);
        if (!wfList.isEmpty()) {
            WorkflowInstance wf = wfList.get(0);
            result.put("workflow", workflowService.getProgress(wf.getId()));
        }
        return result;
    }

    /**
     * 数据查询服务示例 — 按时间范围统计
     */
    public Map<String, Object> getDataSources() {
        Map<String, Object> sources = new LinkedHashMap<>();

        // 请假类型数据源（供下拉选择）
        sources.put("leaveTypes", List.of(
                Map.of("value", "sick", "label", "病假"),
                Map.of("value", "personal", "label", "事假"),
                Map.of("value", "public", "label", "公假")
        ));

        // 请假状态数据源
        sources.put("leaveStatuses", List.of(
                Map.of("value", "pending", "label", "待审批"),
                Map.of("value", "approved", "label", "已通过"),
                Map.of("value", "rejected", "label", "已驳回")
        ));

        // 审批节点数据源
        sources.put("approvalNodes", workflowService.getApprovalNodes());

        return sources;
    }
}
