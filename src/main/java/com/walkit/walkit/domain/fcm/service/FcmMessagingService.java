package com.walkit.walkit.domain.fcm.service;

import com.google.firebase.messaging.*;
import com.walkit.walkit.domain.fcm.entity.FcmToken;
import com.walkit.walkit.domain.fcm.repository.FcmTokenRepository;
import com.walkit.walkit.domain.user.entity.User;
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
            Message msg = Message.builder()
                    .setToken(fcmToken.getToken())
                    .putAllData(data != null ? data : Map.of())
                    .putData("title", title)
                    .putData("body", body)
                    .build();

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


    // 테스트용
    @Transactional
    public String sendNotification(Long userId, String title, String body) {
        FcmToken fcmToken = fcmTokenRepository
                .findTopByUserIdAndEnabledTrueOrderByLastUsedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 FCM 토큰이 없습니다. userId=" + userId));

        String messageId = sendToToken(userId, fcmToken, title, body, Map.of("type", "TEST"));
        return "메시지 전송 성공: " + messageId;
    }

    private String sendToToken(Long userId, FcmToken fcmToken, String title, String body, Map<String, String> data) {
        try {
            Message msg = Message.builder()
                    .setToken(fcmToken.getToken())
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data != null ? data : Map.of())
                    .build();

            String messageId = FirebaseMessaging.getInstance().send(msg);
            log.info("FCM 전송 성공 userId={}, messageId={}", userId, messageId);

            fcmToken.updateLastUsed();
            return messageId;

        } catch (FirebaseMessagingException e) {
            MessagingErrorCode code = e.getMessagingErrorCode();
            log.warn("FCM 전송 실패 userId={}, code={}, token={}",
                    userId, code, maskToken(fcmToken.getToken()), e);

            if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                fcmToken.disable();
            }

            throw new RuntimeException("메시지 전송 실패: " + code, e);
        }
    }

    // 토큰 마스킹 (로그용)
    private String maskToken(String token) {
        if (token == null || token.length() < 10) return "***";
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}
