
package com.walkit.walkit.domain.fcm.entity;

import com.walkit.walkit.domain.BaseTimeEntity;
import com.walkit.walkit.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FcmToken extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 512, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 20)
    private DeviceType deviceType;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(nullable = false)
    private Boolean enabled = true;

    private LocalDateTime lastUsedAt;

    @Builder
    public FcmToken(User user, String token, DeviceType deviceType, String deviceId) {
        this.user = user;
        this.token = token;
        this.deviceType = deviceType;
        this.deviceId = deviceId;
        this.enabled = true;
        this.lastUsedAt = LocalDateTime.now();
    }

    // 활성 / 비활성
    public void disable() {
        this.enabled = false;
    }

    // 마지막 사용 시간 업데이트
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
        this.enabled = true;
    }

    // 계정 전환/기기정보 갱신
    public void reassign(User user, DeviceType deviceType, String deviceId) {
        this.user = user;
        this.deviceType = deviceType;
        this.deviceId = deviceId;
        updateLastUsed();
    }



}

