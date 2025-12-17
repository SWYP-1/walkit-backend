package com.walkit.walkit.domain.user.entity;

import com.walkit.walkit.domain.BaseTimeEntity;
import com.walkit.walkit.domain.character.entity.Character;
import com.walkit.walkit.domain.goal.entity.Goal;
import com.walkit.walkit.domain.user.dto.request.RequestPolicyDto;
import com.walkit.walkit.domain.user.dto.request.RequestUserDto;
import com.walkit.walkit.domain.user.enums.Sex;
import com.walkit.walkit.domain.user.enums.UserRole;
import com.walkit.walkit.common.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_provider_provider_id",
           columnNames = {"auth_provider", "provider_id"}
       ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String name;
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private AuthProvider authProvider;

    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private UserRole role = UserRole.ROLE_USER;

    @Column(unique = true)
    private String nickname;
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private Sex sex;

    private String imageUrl;

    private boolean isMarketingConsent = false;
    private boolean isTermAgreed = false;
    private boolean isPrivacyAgreed = false;

    @Embedded
    private Character character;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    @Column(nullable = false)
    private Integer point = 0;// 포인트 컬럼 임시 추가


    public User updateOauth(String name, String profileImageUrl) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public void updatePolicy(RequestPolicyDto dto) {
        this.isTermAgreed = dto.isTermsAgreed();
        this.isPrivacyAgreed = dto.isPrivacyAgreed();
        this.isMarketingConsent = dto.isMarketingConsent();
    }

    public void update(RequestUserDto dto) {
        this.nickname = dto.getNickname();
        this.birthDate = dto.getBirthDate();
        this.sex = dto.getSex();
    }

    public void updateGoal(Goal goal) {
        this.goal = goal;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void updateMarketingConsent(boolean marketingConsent) {
        this.isMarketingConsent = marketingConsent;
    }


    public void addPoints(int amount) {
        if (amount <= 0) return;
        this.point += amount;
    }

    // == 알림 설정 ==

    // 기기 알림 켜기/끄기 (전체 알림)
    @Builder.Default
    @Column(name = "notification_enabled", nullable = false)
    private Boolean notificationEnabled = true;

    // 목표 달성 알림
    @Builder.Default
    @Column(name = "goal_notification_enabled", nullable = false)
    private Boolean goalNotificationEnabled = true;

    // 새로운 미션 오픈 알림
    @Builder.Default
    @Column(name = "new_mission_notification_enabled", nullable = false)
    private Boolean newMissionNotificationEnabled = true;

    @Column(name = "last_access_at")
    private LocalDateTime lastAccessAt;

    private LocalDateTime inactive48hNotifiedAt;

    public void updateLastAccessAt(LocalDateTime time) {
        this.lastAccessAt = time;
    }


     // 전체 알림 수신 가능 여부
    public boolean canReceiveNotification() {
        return notificationEnabled != null && notificationEnabled;
    }


    // 목표 달성 알림 수신 가능 여부
    public boolean canReceiveGoalNotification() {
        return canReceiveNotification() &&
                goalNotificationEnabled != null &&
                goalNotificationEnabled;
    }

    // 미션 알림 수신 가능 여부
    public boolean canReceiveMissionNotification() {
        return canReceiveNotification() &&
                newMissionNotificationEnabled != null &&
                newMissionNotificationEnabled;
    }

    public void updateNotificationEnabled(Boolean enabled) {
        this.notificationEnabled = enabled;
    }

    public void updateGoalNotificationEnabled(Boolean enabled) {
        this.goalNotificationEnabled = enabled;
    }

    public void updateNewMissionNotificationEnabled(Boolean enabled) {
        this.newMissionNotificationEnabled = enabled;
    }


}


