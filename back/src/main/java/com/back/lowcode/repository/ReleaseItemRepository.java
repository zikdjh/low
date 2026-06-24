package com.back.lowcode.repository;

import com.back.lowcode.entity.ReleaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReleaseItemRepository extends JpaRepository<ReleaseItem, Long> {

    List<ReleaseItem> findByReleaseId(Long releaseId);

    List<ReleaseItem> findByReleaseIdAndItemType(Long releaseId, String itemType);

    void deleteByReleaseId(Long releaseId);
}
