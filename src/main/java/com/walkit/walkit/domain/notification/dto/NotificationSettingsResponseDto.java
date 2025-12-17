package com.walkit.walkit.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationSettingsResponseDto {
    private Boolean notificationEnabled;      // 전체 알림
    private Boolean goalNotificationEnabled;  // 목표 알림
    private Boolean missionNotificationEnabled; // 미션 알림
}
