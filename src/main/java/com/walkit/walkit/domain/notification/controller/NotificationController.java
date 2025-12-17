package com.walkit.walkit.domain.notification.controller;

import com.walkit.walkit.domain.notification.dto.NotificationSettingsRequestDto;
import com.walkit.walkit.domain.notification.dto.NotificationSettingsResponseDto;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {

    private final UserRepository userRepository;

    @GetMapping("/settings")
    public NotificationSettingsResponseDto getSettings(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow();

        return new NotificationSettingsResponseDto(
                user.getNotificationEnabled(),
                user.getGoalNotificationEnabled(),
                user.getNewMissionNotificationEnabled()
        );
    }

    @PatchMapping("/settings")
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

        return new NotificationSettingsResponseDto(
                user.getNotificationEnabled(),
                user.getGoalNotificationEnabled(),
                user.getNewMissionNotificationEnabled()
        );
    }
}
