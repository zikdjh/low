package com.back.repository;

import com.back.entity.po.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByStatus(String status, Pageable pageable);
    
    Page<Notification> findByIsPublishedTrue(Pageable pageable);
    
    List<Notification> findByIsPublishedTrueAndStatusOrderByPublishedAtDesc(String status);
}