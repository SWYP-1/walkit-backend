package com.walkit.walkit.domain.fcm.service;

import com.google.firebase.messaging.*;
import com.walkit.walkit.domain.fcm.entity.DeviceType;
import com.walkit.walkit.domain.fcm.entity.FcmToken;
import com.walkit.walkit.domain.fcm.repository.FcmTokenRepository;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmMessagingService {

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    // 단일 사용자에게 알림 전송 (단일 디바이스 정책: 활성 토큰 1개만 사용)
    @Transactional
    public boolean sendNotification(User user, String title, String body, Map<String, String> data) {
        FcmToken fcmToken = fcmTokenRepository
                .findTopByUserIdAndEnabledTrueOrderByLastUsedAtDesc(user.getId())
                .orElse(null);

        if (fcmToken == null) {
            log.info("활성 FCM 토큰 없음: userId={}", user.getId());
            return false;
        }

        try {
            /*Message msg = Message.builder()
                    .setToken(fcmToken.getToken())
                    .putAllData(data != null ? data : Map.of())
                    .putData("title", title)
                    .putData("body", body)
                    .build();*/

            Message msg = buildMessage(fcmToken, title, body, data);

            String messageId = FirebaseMessaging.getInstance().send(msg);
            log.info("FCM 전송 성공 userId={}, messageId={}", user.getId(), messageId);

            fcmToken.updateLastUsed();
            return true;

        } catch (FirebaseMessagingException e) {
            MessagingErrorCode code = e.getMessagingErrorCode();
            log.warn("FCM 전송 실패 userId={}, code={}, token={}",
                    user.getId(), code, maskToken(fcmToken.getToken()), e);

            // 죽은 토큰 정리
            switch (code) {
                case UNREGISTERED -> {
                    fcmToken.disable();
                    log.info("FCM token unregistered. disabled token={}", fcmToken.getId());
                }
                case INVALID_ARGUMENT -> {
                    log.warn("INVALID_ARGUMENT. token={}, message payload check", fcmToken.getId());
                }
                default -> {
                    log.warn("FCM send failed. code={}", code);
                }
            }

            return false;
        }
    }


    private Message buildMessage(FcmToken token, String title, String body, Map<String, String> data) {
        Map<String, String> payload = new java.util.HashMap<>();
        if (data != null) payload.putAll(data);

        Message.Builder builder = Message.builder()
                .setToken(token.getToken());

        // IOS: Notification로 표시, data에는 데이터만
        if (token.getDeviceType() == DeviceType.IOS) {
            builder.setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build());

            builder.setApnsConfig(
                    ApnsConfig.builder()
                            .putHeader("apns-priority", "10")
                            .setAps(Aps.builder()
                                    .setAlert(ApsAlert.builder()
                                            .setTitle(title)
                                            .setBody(body)
                                            .build())
                                    .setSound("default")
                                    .build())
                            .build()
            );
            builder.putAllData(payload);

        } else {
            // ANDROID: data-only, title/body를 data에 넣어줌
            if (title != null) payload.put("title", title);
            if (body != null) payload.put("body", body);
            builder.putAllData(payload);
        }

        return builder.build();
    }



    @Transactional
    public boolean sendNotification(Long userId, String title, String body) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. userId=" + userId));

        return sendNotification(
                user,
                title,
                body,
                Map.of("type", "TEST")
        );
    }



    // 토큰 마스킹 (로그용)
    private String maskToken(String token) {
        if (token == null || token.length() < 10) return "***";
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}
