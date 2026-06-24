package com.back.lowcode.repository;

import com.back.lowcode.entity.Release;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReleaseRepository extends JpaRepository<Release, Long> {

    List<Release> findByAppCodeOrderByCreatedAtDesc(String appCode);

    Optional<Release> findByAppCodeAndStatus(String appCode, String status);

    Optional<Release> findByAppCodeAndVersion(String appCode, String version);

    boolean existsByAppCodeAndVersion(String appCode, String version);
}
