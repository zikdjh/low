package com.back.service;

import com.back.entity.dto.NotificationRequest;
import com.back.entity.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(NotificationRequest request);

    NotificationResponse updateNotification(Long id, NotificationRequest request);

    NotificationResponse getNotificationById(Long id);

    Page<NotificationResponse> getAllNotifications(Pageable pageable);

    void deleteNotification(Long id);

    NotificationResponse publishNotification(Long id);

    void unpublishNotification(Long id);

    List<NotificationResponse> getPublishedNotifications();
}