package com.back.generated.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 学生请假申请记录，关联学生实体，通过工作流完成审批流程 — DTO
 * 由低代码代码生成器生成 @ 2026-06-25T20:49:37.0509793
 */
@Data
public class LeaveApplicationDTO {

    private Long id;

    /** 学生 */
    @NotNull(message = "学生不能为空")
    
    private Long studentId;

    /** 学生姓名 */
    @NotNull(message = "学生姓名不能为空")
    @Size(max = 64, message = "学生姓名长度不能超过64")
    private String studentName;

    /** 请假类型 */
    @NotNull(message = "请假类型不能为空")
    @Size(max = 32, message = "请假类型长度不能超过32")
    private String leaveType;

    /** 请假原因 */
    @NotNull(message = "请假原因不能为空")
    
    private String reason;

    /** 开始日期 */
    @NotNull(message = "开始日期不能为空")
    
    private LocalDate startDate;

    /** 结束日期 */
    @NotNull(message = "结束日期不能为空")
    
    private LocalDate endDate;

    /** 请假天数 */
    @NotNull(message = "请假天数不能为空")
    
    private Integer leaveDays;

    /** 审批状态 */
    @NotNull(message = "审批状态不能为空")
    @Size(max = 32, message = "审批状态长度不能超过32")
    private String status;

}
