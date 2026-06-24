package com.back.lowcode.repository;

import com.back.lowcode.entity.PageSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageSchemaRepository extends JpaRepository<PageSchema, Long> {

    Optional<PageSchema> findByPageCode(String pageCode);

    List<PageSchema> findByStatus(String status);

    List<PageSchema> findByEntityCode(String entityCode);

    boolean existsByPageCode(String pageCode);

    List<PageSchema> findByAppCode(String appCode);

    List<PageSchema> findByAppCodeAndStatus(String appCode, String status);
}