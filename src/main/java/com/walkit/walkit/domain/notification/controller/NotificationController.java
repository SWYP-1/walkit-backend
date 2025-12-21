package com.walkit.walkit.domain.notification.controller;

import com.walkit.walkit.domain.notification.dto.NotificationResponseDto;
import com.walkit.walkit.domain.notification.dto.NotificationSettingsRequestDto;
import com.walkit.walkit.domain.notification.dto.NotificationSettingsResponseDto;
import com.walkit.walkit.domain.notification.service.NotificationService;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @GetMapping("/setting")
    public NotificationSettingsResponseDto getSettings(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow();

        return new NotificationSettingsResponseDto(
                user.getNotificationEnabled(),
                user.getGoalNotificationEnabled(),
                user.getNewMissionNotificationEnabled(),
                user.getFriendNotificationEnabled(),
                user.getMarketingPushEnabled()
        );
    }

    @PatchMapping("/setting")
    @Transactional
    public NotificationSettingsResponseDto update(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody NotificationSettingsRequestDto req
    ) {
        User user = userRepository.findById(principal.getUserId()).orElseThrow();

        if (req.getNotificationEnabled() != null)
            user.updateNotificationEnabled(req.getNotificationEnabled());

        if (req.getGoalNotificationEnabled() != null)
            user.updateGoalNotificationEnabled(req.getGoalNotificationEnabled());

        if (req.getNewMissionNotificationEnabled() != null)
            user.updateNewMissionNotificationEnabled(req.getNewMissionNotificationEnabled());

        if (req.getFriendNotificationEnabled() != null)
            user.updateFriendNotificationEnabled(req.getFriendNotificationEnabled());

        if (req.getMarketingPushEnabled() != null)
            user.updateMarketingPushEnabled(req.getMarketingPushEnabled());

        return new NotificationSettingsResponseDto(
                user.getNotificationEnabled(),
                user.getGoalNotificationEnabled(),
                user.getNewMissionNotificationEnabled(),
                user.getFriendNotificationEnabled(),
                user.getMarketingPushEnabled()
        );
    }

    @GetMapping("/list")
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "20") int limit
    ) {
        // 안전장치 (원하는 값으로 조절)
        int safeLimit = Math.min(Math.max(limit, 1), 100);

        return ResponseEntity.ok(
                notificationService.getNotifications(userPrincipal.getUserId(), safeLimit)
        );
    }
}

