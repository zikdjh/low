package com.back.generated.service;

import com.back.generated.dto.StudentDTO;
import com.back.generated.entity.Student;
import com.back.generated.repository.StudentRepository;
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
 * 学生基本信息，包含学号、班级、院系、联系方式等 Service
 * 由低代码代码生成器生成 @ 2026-06-25T21:02:06.2970011
 */
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository repository;

    public Page<Student> page(StudentDTO query, int pageNum, int pageSize) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        PageRequest pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize, sort);
        Specification<Student> spec = buildSpec(query);
        return repository.findAll(spec, pageable);
    }

    public Student getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public Student create(StudentDTO dto) {
        Student entity = new Student();
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }

    @Transactional
    public Student update(Long id, StudentDTO dto) {
        Student entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("学生信息不存在: " + id));
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

    private Specification<Student> buildSpec(StudentDTO query) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query == null) return cb.conjunction();
            if (query.getStudentNo() != null && !query.getStudentNo().isEmpty()) {
                predicates.add(cb.like(root.get("studentNo"), "%" + query.getStudentNo() + "%"));
            }
            if (query.getName() != null && !query.getName().isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + query.getName() + "%"));
            }
            if (query.getClassName() != null && !query.getClassName().isEmpty()) {
                predicates.add(cb.like(root.get("className"), "%" + query.getClassName() + "%"));
            }
            if (query.getStatus() != null && !query.getStatus().isEmpty()) {
                predicates.add(cb.like(root.get("status"), "%" + query.getStatus() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
