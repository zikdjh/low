package com.back.lowcode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作流任务 — 记录每个审批节点的审批信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lc_workflow_task")
public class WorkflowTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属工作流实例ID */
    @Column(name = "instance_id", nullable = false)
    private Long instanceId;

    /** 节点名称，如 "辅导员审批"、"系主任审批" */
    @Column(name = "node_name", nullable = false, length = 64)
    private String nodeName;

    /** 节点序号（0, 1, 2...） */
    @Column(name = "node_order", nullable = false)
    private Integer nodeOrder;

    /** 审批人角色编码，如 "counselor"、"dept_head" */
    @Column(name = "assignee_role", nullable = false, length = 32)
    private String assigneeRole;

    /** 实际审批人ID（审批后填充） */
    @Column(name = "assignee_id")
    private Long assigneeId;

    /** 审批人名称 */
    @Column(name = "assignee_name", length = 64)
    private String assigneeName;

    /**
     * 任务状态:
     * pending — 待审批
     * approved — 已通过
     * rejected — 已驳回
     * skipped — 已跳过
     */
    @Column(length = 16, nullable = false)
    @Builder.Default
    private String status = "pending";

    /** 审批意见 */
    @Column(length = 512)
    private String comment;

    /** 审批时间 */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
