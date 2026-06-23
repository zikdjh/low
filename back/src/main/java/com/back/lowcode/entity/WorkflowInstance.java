package com.back.lowcode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作流实例 — 记录每次请假申请的完整流程
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lc_workflow_instance")
public class WorkflowInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联的业务数据ID */
    @Column(name = "business_id", nullable = false)
    private Long businessId;

    /** 关联的业务实体编码，如 "leave_application" */
    @Column(name = "business_code", nullable = false, length = 64)
    private String businessCode;

    /** 流程定义编码，如 "leave_approval" */
    @Column(name = "workflow_code", nullable = false, length = 64)
    private String workflowCode;

    /** 流程名称 */
    @Column(length = 128)
    private String workflowName;

    /**
     * 流程状态:
     * pending — 流程进行中
     * approved — 全部审批通过
     * rejected — 被驳回
     * cancelled — 申请人撤回
     * archived — 已归档
     */
    @Column(length = 16, nullable = false)
    @Builder.Default
    private String status = "pending";

    /** 发起人用户ID */
    @Column(name = "initiator_id", nullable = false)
    private Long initiatorId;

    /** 发起人用户名 */
    @Column(name = "initiator_name", length = 64)
    private String initiatorName;

    /** 当前审批节点索引（从0开始） */
    @Column(name = "current_node_index")
    @Builder.Default
    private Integer currentNodeIndex = 0;

    /** 总审批节点数 */
    @Column(name = "total_nodes")
    @Builder.Default
    private Integer totalNodes = 0;

    /** 完成时间 */
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

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
