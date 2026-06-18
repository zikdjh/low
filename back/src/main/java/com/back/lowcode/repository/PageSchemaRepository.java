package com.back.lowcode.repository;

import com.back.lowcode.entity.PageSchema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PageSchemaRepository extends JpaRepository<PageSchema, Long> {

    Optional<PageSchema> findByCode(String code);

    Optional<PageSchema> findByCodeAndStatus(String code, String status);

    List<PageSchema> findAllByStatusOrderByUpdatedAtDesc(String status);
}
