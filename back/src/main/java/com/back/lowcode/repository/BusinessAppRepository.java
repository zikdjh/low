package com.back.lowcode.repository;

import com.back.lowcode.entity.BusinessApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessAppRepository extends JpaRepository<BusinessApp, Long> {

    Optional<BusinessApp> findByCode(String code);

    boolean existsByCode(String code);
}
