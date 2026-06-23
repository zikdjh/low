package com.back.lowcode.repository;

import com.back.lowcode.entity.WorkflowInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {

    /** 根据业务数据和编码查找 */
    List<WorkflowInstance> findByBusinessIdAndBusinessCode(Long businessId, String businessCode);

    /** 查询某用户的发起记录 */
    Page<WorkflowInstance> findByInitiatorIdOrderByCreatedAtDesc(Long initiatorId, Pageable pageable);

    /** 查询某用户发起的所有记录 */
    List<WorkflowInstance> findByInitiatorId(Long initiatorId);

    /** 根据状态查询 */
    Page<WorkflowInstance> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    /** 全量分页 */
    Page<WorkflowInstance> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
