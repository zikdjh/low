package com.back.generated.service;

import com.back.generated.dto.LeaveApplicationDTO;
import com.back.generated.entity.LeaveApplication;
import com.back.generated.repository.LeaveApplicationRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 学生请假申请记录，关联学生实体，通过工作流完成审批流程 Service
 * 由低代码代码生成器生成 @ 2026-06-25T20:49:37.0509793
 */
@Service
@RequiredArgsConstructor
public class LeaveApplicationService {

    private final LeaveApplicationRepository repository;

    public Page<LeaveApplication> page(LeaveApplicationDTO query, int pageNum, int pageSize) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        PageRequest pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize, sort);
        Specification<LeaveApplication> spec = buildSpec(query);
        return repository.findAll(spec, pageable);
    }

    public LeaveApplication getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public LeaveApplication create(LeaveApplicationDTO dto) {
        LeaveApplication entity = new LeaveApplication();
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }

    @Transactional
    public LeaveApplication update(Long id, LeaveApplicationDTO dto) {
        LeaveApplication entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("请假申请不存在: " + id));
        BeanUtils.copyProperties(dto, entity, "id");
        return repository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
        repository.deleteAllById(ids);
    }

    private Specification<LeaveApplication> buildSpec(LeaveApplicationDTO query) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query == null) return cb.conjunction();
            if (query.getStudentId() != null) {
                predicates.add(cb.equal(root.get("studentId"), query.getStudentId()));
            }
            if (query.getStudentName() != null && !query.getStudentName().isEmpty()) {
                predicates.add(cb.like(root.get("studentName"), "%" + query.getStudentName() + "%"));
            }
            if (query.getLeaveType() != null && !query.getLeaveType().isEmpty()) {
                predicates.add(cb.like(root.get("leaveType"), "%" + query.getLeaveType() + "%"));
            }
            if (query.getStatus() != null && !query.getStatus().isEmpty()) {
                predicates.add(cb.like(root.get("status"), "%" + query.getStatus() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
