package com.walkit.walkit.domain.fcm.service;

import com.walkit.walkit.domain.fcm.entity.DeviceType;
import com.walkit.walkit.domain.fcm.entity.FcmToken;
import com.walkit.walkit.domain.fcm.repository.FcmTokenRepository;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    // FCM 토큰 등록 또는 갱신
    @Transactional
    public void registerToken(Long userId, String token, DeviceType deviceType,String deviceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 단일 디바이스 정책: 기존 활성 토큰 전부 비활성화
        fcmTokenRepository.disableAllActiveByUserId(userId);

        // 이미 존재하는 토큰인지 확인
        FcmToken existingToken = fcmTokenRepository.findByToken(token)
                .orElse(null);

        if (existingToken != null) {
            // 같은 토큰이면 재활성 + lastUsed 갱신 + 기기정보 갱신
            existingToken.updateLastUsed();
            log.info("FCM 토큰 갱신: userId={}", userId);
        } else {
            // 새 토큰 등록
            FcmToken newToken = FcmToken.builder()
                    .user(user)
                    .token(token)
                    .deviceType(deviceType)
                    .deviceId(deviceId)
                    .build();

            fcmTokenRepository.save(newToken);

            log.info("FCM 토큰 등록: userId={}, deviceType={}", userId, deviceType);
        }
    }

    // Fcm 토큰 삭제
    @Transactional
    public void deleteToken(String token) {
        fcmTokenRepository.findByToken(token)
                .ifPresent(fcmToken -> {
                    fcmToken.disable();
                    log.info("FCM 토큰 비활성화: userId={}", fcmToken.getUser().getId());
                });
    }


}