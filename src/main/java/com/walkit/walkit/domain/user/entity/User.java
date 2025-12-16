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
}
