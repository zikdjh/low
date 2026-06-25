package com.back.generated.repository;

import com.back.generated.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 学生基本信息，包含学号、班级、院系、联系方式等 Repository
 * 由低代码代码生成器生成 @ 2026-06-25T21:02:06.2970011
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
}
