package com.back.generated.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生请假申请记录，关联学生实体，通过工作流完成审批流程 — 由低代码代码生成器生成
 * 源实体编码: leave_application
 * 生成时间: 2026-06-25T20:49:37.0509793
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lc_leave_application")
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 学生 */
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    /** 学生姓名 */
    @Column(name = "student_name", nullable = false, length = 64)
    private String studentName;

    /** 请假类型 */
    @Column(name = "leave_type", nullable = false, length = 32)
    private String leaveType;

    /** 请假原因 */
    @Column(name = "reason", nullable = false)
    private String reason;

    /** 开始日期 */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** 结束日期 */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /** 请假天数 */
    @Column(name = "leave_days", nullable = false)
    private Integer leaveDays;

    /** 审批状态 */
    @Column(name = "status", nullable = false, length = 32)
    private String status;

    /** 创建时间 */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
