package com.back.generated.repository;

import com.back.generated.entity.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 学生请假申请记录，关联学生实体，通过工作流完成审批流程 Repository
 * 由低代码代码生成器生成 @ 2026-06-25T20:49:37.0509793
 */
@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long>, JpaSpecificationExecutor<LeaveApplication> {
}
