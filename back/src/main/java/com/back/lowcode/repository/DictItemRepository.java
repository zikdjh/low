package com.back.lowcode.repository;

import com.back.lowcode.entity.DictItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictItemRepository extends JpaRepository<DictItem, Long> {

    List<DictItem> findByDictCodeAndStatusOrderBySortOrder(String dictCode, String status);

    List<DictItem> findByDictCode(String dictCode);

    void deleteByDictCode(String dictCode);

    boolean existsByDictCodeAndItemKey(String dictCode, String itemKey);
}