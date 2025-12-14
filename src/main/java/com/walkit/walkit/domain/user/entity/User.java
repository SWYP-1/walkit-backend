package com.walkit.walkit.domain.user.entity;

import com.walkit.walkit.domain.BaseTimeEntity;
import com.walkit.walkit.domain.user.dto.request.RequestPolicyDto;
import com.walkit.walkit.domain.user.dto.request.RequestUserDto;
import com.walkit.walkit.domain.user.enums.Asset;
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
    private UserRole role = UserRole.ROLE_USER;

    private String nickname;
    private int birthYear;
    private LocalDate birthDate;
    private Sex sex;
    private String imageUrl;

    private boolean isSubscribed;
    private boolean isTermAgreed;
    private boolean isPrivacyAgreed;

    private int characterLevel;
    private Asset asset;

    public User updateOauth(String name, String profileImageUrl) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public void updateBirthYear(int year) {
        this.birthYear = year;
    }

    public void updatePolicy(RequestPolicyDto dto) {
        this.isTermAgreed = dto.isTermsAgreed();
        this.isPrivacyAgreed = dto.isPrivacyAgreed();
    }

    public void updateIsSubscribed(boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
    }

    public void update(RequestUserDto dto) {
        this.nickname = dto.getName();
        this.birthDate = dto.getBirthDate();
        this.sex = dto.getSex();
    }

    public void updateCharacterLevel(int level) {
        this.characterLevel = level;
    }

    public void updateAsset(Asset asset) {
        this.asset = asset;
    }
}
