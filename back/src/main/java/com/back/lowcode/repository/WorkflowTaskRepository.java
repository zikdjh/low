package com.back.lowcode.repository;

import com.back.lowcode.entity.WorkflowTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, Long> {

    /** 查找某实例下所有任务，按序号排序 */
    List<WorkflowTask> findByInstanceIdOrderByNodeOrderAsc(Long instanceId);

    /** 查找某实例下待审批的任务 */
    List<WorkflowTask> findByInstanceIdAndStatusOrderByNodeOrderAsc(Long instanceId, String status);

    /** 查找某实例下当前节点 */
    Optional<WorkflowTask> findByInstanceIdAndNodeOrder(Long instanceId, Integer nodeOrder);

    /** 查找某用户待审批的任务（通过角色） */
    List<WorkflowTask> findByAssigneeRoleAndStatusOrderByCreatedAtDesc(String assigneeRole, String status);

    /** 查找某用户已审批的任务 */
    List<WorkflowTask> findByAssigneeIdAndStatusInOrderByProcessedAtDesc(Long assigneeId, List<String> statuses);
}
