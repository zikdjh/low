package com.back.generated.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生基本信息，包含学号、班级、院系、联系方式等 — 由低代码代码生成器生成
 * 源实体编码: student
 * 生成时间: 2026-06-25T21:02:06.2970011
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lc_student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 学号 */
    @Column(name = "student_no", nullable = false, length = 20)
    private String studentNo;

    /** 姓名 */
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    /** 性别 */
    @Column(name = "gender", length = 8)
    private String gender;

    /** 班级 */
    @Column(name = "class_name", length = 64)
    private String className;

    /** 年级 */
    @Column(name = "grade", length = 16)
    private String grade;

    /** 院系 */
    @Column(name = "department", length = 64)
    private String department;

    /** 联系电话 */
    @Column(name = "phone", length = 20)
    private String phone;

    /** 邮箱 */
    @Column(name = "email", length = 128)
    private String email;

    /** 关联用户 */
    @Column(name = "user_id")
    private Long userId;

    /** 学籍状态 */
    @Column(name = "status", nullable = false, length = 16)
    private String status;

    /** 创建时间 */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
