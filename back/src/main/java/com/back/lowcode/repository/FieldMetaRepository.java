package com.back.lowcode.repository;

import com.back.lowcode.entity.FieldMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldMetaRepository extends JpaRepository<FieldMeta, Long> {

    List<FieldMeta> findByEntityIdOrderBySortOrderAsc(Long entityId);

    void deleteByEntityId(Long entityId);

    boolean existsByEntityIdAndCode(Long entityId, String code);
}
