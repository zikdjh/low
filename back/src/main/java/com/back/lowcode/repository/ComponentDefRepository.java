package com.back.lowcode.repository;

import com.back.lowcode.entity.ComponentDef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentDefRepository extends JpaRepository<ComponentDef, Long> {

    Optional<ComponentDef> findByCompKey(String compKey);

    List<ComponentDef> findByStatus(String status);

    List<ComponentDef> findByCategoryAndStatus(String category, String status);

    List<ComponentDef> findByIsSystem(Boolean isSystem);

    boolean existsByCompKey(String compKey);
}