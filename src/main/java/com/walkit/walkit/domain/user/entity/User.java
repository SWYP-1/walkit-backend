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
import java.util.Objects;


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

    private String imageUrl;

    private boolean isMarketingConsent = false;

    @Embedded
    private UserAgreement userAgreement;

    @Embedded
    private Character character;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    @Column(nullable = false)
    private int point = 0;// 포인트 컬럼 임시 추가

    public User updateOauth(String name, String profileImageUrl) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public void updatePolicy(RequestPolicyDto dto) {
        this.userAgreement.update(dto);
        this.isMarketingConsent = dto.isMarketingConsent();
    }

    public void update(RequestUserDto dto) {
        this.nickname = dto.getNickname();
        this.birthDate = dto.getBirthDate();
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
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

    // 친구 팔로우 알림
    @Builder.Default
    @Column(name = "friend_notification_enabled", nullable = false)
    private Boolean friendNotificationEnabled = true;

    // 마케팅 푸시 알림 (프로모션/이벤트)
    @Builder.Default
    @Column(name = "marketing_push_enabled", nullable = false)
    private Boolean marketingPushEnabled = true;

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

    // 친구 알림 수신 가능 여부
    public boolean canReceiveFriendNotification() {
        return canReceiveNotification() &&
                friendNotificationEnabled != null &&
                friendNotificationEnabled;
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

    public void updateFriendNotificationEnabled(Boolean enabled) { this.friendNotificationEnabled = enabled;}

    public void updateMarketingPushEnabled(Boolean enabled) { this.marketingPushEnabled = enabled; }


}


