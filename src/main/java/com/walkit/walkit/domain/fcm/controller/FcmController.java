package com.walkit.walkit.domain.fcm.controller;

import com.walkit.walkit.domain.fcm.dto.FcmMessageRequestDto;
import com.walkit.walkit.domain.fcm.dto.FcmTokenRegisterRequestDto;

import com.walkit.walkit.domain.fcm.service.FcmMessagingService;
import com.walkit.walkit.domain.fcm.service.FcmTokenService;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;



@RequiredArgsConstructor
@RequestMapping("/fcm")
@RestController
@Slf4j
public class FcmController {

    private final FcmMessagingService fcmMessagingService;
    private final FcmTokenService fcmTokenService;


    // 토큰 등록
    @PostMapping("/token")
    public ResponseEntity<Void> register(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FcmTokenRegisterRequestDto request
    ) {
        fcmTokenService.registerToken(
                principal.getUserId(),
                request.getToken(),
                request.getDeviceType(),
                request.getDeviceId()

        );

        log.info("FCM 토큰 등록 성공: userId={}", principal.getUserId());
        return ResponseEntity.noContent().build();
    }


   /*
    @PostMapping("/test")
    public String testSend(@AuthenticationPrincipal UserPrincipal principal,
                           @RequestBody FcmMessageRequestDto req) {
        return fcmMessagingService.sendNotification(principal.getUserId(), req.getTitle(), req.getBody());
    }*/




}