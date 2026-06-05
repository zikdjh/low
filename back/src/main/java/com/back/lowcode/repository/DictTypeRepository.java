package com.back.lowcode.repository;

import com.back.lowcode.entity.DictType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DictTypeRepository extends JpaRepository<DictType, Long> {

    Optional<DictType> findByDictCode(String dictCode);

    List<DictType> findByStatus(String status);

    boolean existsByDictCode(String dictCode);
}