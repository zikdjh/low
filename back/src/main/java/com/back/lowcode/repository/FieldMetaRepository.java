package com.back.lowcode.repository;

import com.back.lowcode.entity.FieldMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldMetaRepository extends JpaRepository<FieldMeta, Long> {

    List<FieldMeta> findByEntityIdOrderBySortOrderAsc(Long entityId);

    void deleteByEntityId(Long entityId);

    boolean existsByEntityIdAndCode(Long entityId, String code);

    /** 批量统计实体字段数量，返回 [entityId, count] */
    @Query("SELECT f.entityId, COUNT(f.id) FROM FieldMeta f WHERE f.entityId IN :entityIds GROUP BY f.entityId")
    List<Object[]> countByEntityIdIn(@Param("entityIds") List<Long> entityIds);
}
