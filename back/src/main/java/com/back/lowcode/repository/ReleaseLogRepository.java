package com.back.lowcode.repository;

import com.back.lowcode.entity.ReleaseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReleaseLogRepository extends JpaRepository<ReleaseLog, Long> {

    List<ReleaseLog> findByReleaseIdOrderByCreatedAtAsc(Long releaseId);
}
