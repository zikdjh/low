package com.back.lowcode.repository;

import com.back.lowcode.entity.EntityMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntityMetaRepository extends JpaRepository<EntityMeta, Long>,
        JpaSpecificationExecutor<EntityMeta> {

    Optional<EntityMeta> findByCode(String code);

    Optional<EntityMeta> findByTableName(String tableName);

    boolean existsByCode(String code);

    @Query("SELECT e FROM EntityMeta e WHERE " +
            "(:keyword IS NULL OR e.name LIKE %:keyword% OR e.code LIKE %:keyword%) " +
            "ORDER BY e.updatedAt DESC")
    List<EntityMeta> search(@Param("keyword") String keyword);
}
