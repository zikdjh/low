package com.back.generated.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 学生基本信息，包含学号、班级、院系、联系方式等 — DTO
 * 由低代码代码生成器生成 @ 2026-06-25T21:02:06.2970011
 */
@Data
public class StudentDTO {

    private Long id;

    /** 学号 */
    @NotNull(message = "学号不能为空")
    @Size(max = 20, message = "学号长度不能超过20")
    private String studentNo;

    /** 姓名 */
    @NotNull(message = "姓名不能为空")
    @Size(max = 64, message = "姓名长度不能超过64")
    private String name;

    /** 性别 */
    
    @Size(max = 8, message = "性别长度不能超过8")
    private String gender;

    /** 班级 */
    
    @Size(max = 64, message = "班级长度不能超过64")
    private String className;

    /** 年级 */
    
    @Size(max = 16, message = "年级长度不能超过16")
    private String grade;

    /** 院系 */
    
    @Size(max = 64, message = "院系长度不能超过64")
    private String department;

    /** 联系电话 */
    
    @Size(max = 20, message = "联系电话长度不能超过20")
    private String phone;

    /** 邮箱 */
    
    @Size(max = 128, message = "邮箱长度不能超过128")
    private String email;

    /** 关联用户 */
    
    
    private Long userId;

    /** 学籍状态 */
    @NotNull(message = "学籍状态不能为空")
    @Size(max = 16, message = "学籍状态长度不能超过16")
    private String status;

}
