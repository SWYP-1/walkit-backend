package com.walkit.walkit.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class NotificationResponseDto {

    private Long notificationId;
    private String type;
    private String title;
    private String body;
    private Long senderId;          // 시스템 알림이면 null
    private String senderNickname;  // 시스템 알림이면 null
    private String targetId;
    private boolean isRead;
    private LocalDateTime createdAt;


}

