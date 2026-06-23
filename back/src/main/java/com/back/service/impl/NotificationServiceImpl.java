package com.back.service.impl;

import com.back.entity.dto.NotificationRequest;
import com.back.entity.dto.NotificationResponse;
import com.back.entity.po.Notification;
import com.back.repository.NotificationRepository;
import com.back.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .type(request.getType() != null ? request.getType() : "info")
                .targetUsers(request.getTargetUsers())
                .status("active")
                .isPublished(false)
                .build();
        
        Notification saved = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public NotificationResponse updateNotification(Long id, NotificationRequest request) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        if (request.getType() != null) {
            notification.setType(request.getType());
        }
        notification.setTargetUsers(request.getTargetUsers());
        
        Notification updated = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(updated);
    }

    @Override
    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        return NotificationResponse.fromEntity(notification);
    }

    @Override
    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable)
                .map(NotificationResponse::fromEntity);
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new RuntimeException("通知不存在");
        }
        notificationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public NotificationResponse publishNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        
        notification.setIsPublished(true);
        notification.setPublishedAt(LocalDateTime.now());
        notification.setStatus("active");
        
        Notification updated = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void unpublishNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        
        notification.setIsPublished(false);
        notification.setPublishedAt(null);
        
        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationResponse> getPublishedNotifications() {
        return notificationRepository.findByIsPublishedTrueAndStatusOrderByPublishedAtDesc("active")
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }
}