package com.walkit.walkit.domain.notification.dto;

import lombok.Getter;

@Getter
public class NotificationSettingsRequestDto {
    private Boolean notificationEnabled;
    private Boolean goalNotificationEnabled;
    private Boolean newMissionNotificationEnabled;
    private Boolean friendNotificationEnabled;   // 팔로우 알림
    private Boolean marketingPushEnabled;   // 마케팅 수신 동의

}

