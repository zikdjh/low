package com.back.entity.dto;

import com.back.entity.po.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    
    private String title;
    
    private String content;
    
    private String type;
    
    private String status;
    
    private String targetUsers;
    
    private Boolean isPublished;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime publishedAt;

    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .status(notification.getStatus())
                .targetUsers(notification.getTargetUsers())
                .isPublished(notification.getIsPublished())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .publishedAt(notification.getPublishedAt())
                .build();
    }
}