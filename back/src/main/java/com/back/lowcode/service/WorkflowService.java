package com.back.lowcode.service;

import com.back.entity.po.User;
import com.back.lowcode.entity.WorkflowInstance;
import com.back.lowcode.entity.WorkflowTask;
import com.back.lowcode.repository.WorkflowInstanceRepository;
import com.back.lowcode.repository.WorkflowTaskRepository;
import com.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 工作流引擎服务 — 核心流转逻辑
 * 实现请假管理场景的：发起 → 辅导员审批 → 系主任审批 → 归档
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowTaskRepository taskRepository;
    private final UserRepository userRepository;

    /**
     * 流程节点定义 (请假审批流程)
     * 节点0: 辅导员审批 (角色 counselor)
     * 节点1: 系主任审批 (角色 dept_head)
     */
    private static final List<Map<String, String>> LEAVE_APPROVAL_NODES = List.of(
            Map.of("name", "辅导员审批", "role", "counselor"),
            Map.of("name", "系主任审批", "role", "dept_head")
    );

    /**
     * 发起流程
     */
    @Transactional
    public WorkflowInstance startWorkflow(String businessCode, Long businessId, String workflowCode,
                                          String workflowName, Long initiatorId, String initiatorName) {
        // 创建流程实例
        WorkflowInstance instance = WorkflowInstance.builder()
                .businessId(businessId)
                .businessCode(businessCode)
                .workflowCode(workflowCode)
                .workflowName(workflowName)
                .initiatorId(initiatorId)
                .initiatorName(initiatorName)
                .status("pending")
                .currentNodeIndex(0)
                .totalNodes(LEAVE_APPROVAL_NODES.size())
                .build();
        instance = instanceRepository.save(instance);

        // 创建第一个审批节点任务
        Map<String, String> firstNode = LEAVE_APPROVAL_NODES.get(0);
        WorkflowTask task = WorkflowTask.builder()
                .instanceId(instance.getId())
                .nodeName(firstNode.get("name"))
                .nodeOrder(0)
                .assigneeRole(firstNode.get("role"))
                .status("pending")
                .build();
        taskRepository.save(task);

        log.info("工作流启动: instanceId={}, workflowCode={}, initiator={}", instance.getId(), workflowCode, initiatorName);
        return instance;
    }

    /**
     * 审批通过 — 流转到下一节点或完成
     */
    @Transactional
    public Map<String, Object> approve(Long instanceId, Long userId, String username, String comment) {
        WorkflowInstance instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("流程实例不存在: " + instanceId));

        if (!"pending".equals(instance.getStatus())) {
            throw new IllegalStateException("该流程已结束，无法审批");
        }

        // 处理当前节点
        int currentNode = instance.getCurrentNodeIndex();
        WorkflowTask task = taskRepository.findByInstanceIdAndNodeOrder(instanceId, currentNode)
                .orElseThrow(() -> new IllegalStateException("审批节点不存在"));

        // 检查权限：当前用户必须有对应角色
        User user = userRepository.findById(userId).orElse(null);
        boolean hasRole = false;
        if (user != null) {
            hasRole = user.getRoles().stream()
                    .anyMatch(r -> r.getCode().equals(task.getAssigneeRole()));
        }
        if (!hasRole) {
            throw new IllegalStateException("您没有审批权限，需要角色: " + task.getAssigneeRole());
        }

        // 更新任务状态
        task.setStatus("approved");
        task.setAssigneeId(userId);
        task.setAssigneeName(username);
        task.setComment(comment);
        task.setProcessedAt(LocalDateTime.now());
        taskRepository.save(task);

        // 判断是否还有下一个节点
        if (currentNode + 1 < LEAVE_APPROVAL_NODES.size()) {
            // 流转到下一节点
            instance.setCurrentNodeIndex(currentNode + 1);
            Map<String, String> nextNode = LEAVE_APPROVAL_NODES.get(currentNode + 1);
            WorkflowTask nextTask = WorkflowTask.builder()
                    .instanceId(instanceId)
                    .nodeName(nextNode.get("name"))
                    .nodeOrder(currentNode + 1)
                    .assigneeRole(nextNode.get("role"))
                    .status("pending")
                    .build();
            taskRepository.save(nextTask);
            instanceRepository.save(instance);
            log.info("工作流流转: instanceId={} → node={} ({})", instanceId, currentNode + 1, nextNode.get("name"));
        } else {
            // 全部审批通过
            instance.setStatus("approved");
            instance.setFinishedAt(LocalDateTime.now());
            instanceRepository.save(instance);
            log.info("工作流完成(通过): instanceId={}", instanceId);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("instance", instance);
        result.put("task", task);
        result.put("message", "审批通过");
        return result;
    }

    /**
     * 审批驳回 — 流程终止
     */
    @Transactional
    public Map<String, Object> reject(Long instanceId, Long userId, String username, String comment) {
        WorkflowInstance instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("流程实例不存在: " + instanceId));

        if (!"pending".equals(instance.getStatus())) {
            throw new IllegalStateException("该流程已结束，无法审批");
        }

        int currentNode = instance.getCurrentNodeIndex();
        WorkflowTask task = taskRepository.findByInstanceIdAndNodeOrder(instanceId, currentNode)
                .orElseThrow(() -> new IllegalStateException("审批节点不存在"));

        // 权限检查
        User user = userRepository.findById(userId).orElse(null);
        boolean hasRole = false;
        if (user != null) {
            hasRole = user.getRoles().stream()
                    .anyMatch(r -> r.getCode().equals(task.getAssigneeRole()));
        }
        if (!hasRole) {
            throw new IllegalStateException("您没有审批权限");
        }

        // 若 comment 为空给默认值
        String finalComment = (comment == null || comment.trim().isEmpty()) ? "驳回" : comment;

        task.setStatus("rejected");
        task.setAssigneeId(userId);
        task.setAssigneeName(username);
        task.setComment(finalComment);
        task.setProcessedAt(LocalDateTime.now());
        taskRepository.save(task);

        instance.setStatus("rejected");
        instance.setFinishedAt(LocalDateTime.now());
        instanceRepository.save(instance);

        log.info("工作流驳回: instanceId={}, rejectedBy={}", instanceId, username);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("instance", instance);
        result.put("task", task);
        result.put("message", "已驳回");
        return result;
    }

    /**
     * 撤回申请 — 仅发起人可操作
     */
    @Transactional
    public WorkflowInstance cancel(Long instanceId, Long userId) {
        WorkflowInstance instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("流程实例不存在"));

        if (!instance.getInitiatorId().equals(userId)) {
            throw new IllegalStateException("只有发起人才能撤回");
        }
        if (!"pending".equals(instance.getStatus())) {
            throw new IllegalStateException("当前流程状态不允许撤回");
        }

        instance.setStatus("cancelled");
        instance.setFinishedAt(LocalDateTime.now());
        return instanceRepository.save(instance);
    }

    /**
     * 查询流程进度
     */
    public Map<String, Object> getProgress(Long instanceId) {
        WorkflowInstance instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("流程实例不存在"));

        List<WorkflowTask> tasks = taskRepository.findByInstanceIdOrderByNodeOrderAsc(instanceId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("instance", instance);
        result.put("tasks", tasks);
        result.put("currentNode", instance.getCurrentNodeIndex());
        return result;
    }

    /**
     * 查询用户发起的工作流列表
     */
    public Page<WorkflowInstance> getMyApplications(Long userId, int page, int pageSize) {
        return instanceRepository.findByInitiatorIdOrderByCreatedAtDesc(userId, PageRequest.of(page - 1, pageSize));
    }

    /**
     * 查询某角色的待审批任务
     */
    public List<WorkflowInstance> getPendingApprovals(String roleCode) {
        List<WorkflowTask> pendingTasks = taskRepository.findByAssigneeRoleAndStatusOrderByCreatedAtDesc(roleCode, "pending");
        List<Long> instanceIds = pendingTasks.stream()
                .map(WorkflowTask::getInstanceId)
                .distinct()
                .toList();
        if (instanceIds.isEmpty()) {
            return List.of();
        }
        return instanceRepository.findAllById(instanceIds);
    }

    /**
     * 获取审批节点定义（用于前端展示流程进度）
     */
    public List<Map<String, String>> getApprovalNodes() {
        return LEAVE_APPROVAL_NODES;
    }
}
