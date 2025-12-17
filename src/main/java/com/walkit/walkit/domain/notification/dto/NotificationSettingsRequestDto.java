package com.walkit.walkit.domain.notification.dto;

import lombok.Getter;

@Getter
public class NotificationSettingsRequestDto {
    private Boolean notificationEnabled;
    private Boolean goalNotificationEnabled;
    private Boolean newMissionNotificationEnabled;
}

