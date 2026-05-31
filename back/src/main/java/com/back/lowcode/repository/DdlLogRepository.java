package com.back.lowcode.repository;

import com.back.lowcode.entity.DdlLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DdlLogRepository extends JpaRepository<DdlLog, Long> {

    List<DdlLog> findByEntityIdOrderByExecutedAtDesc(Long entityId);
}
