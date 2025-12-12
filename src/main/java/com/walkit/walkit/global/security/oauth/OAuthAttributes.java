package com.walkit.walkit.global.security.oauth;

import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.entity.UserRole;
import com.walkit.walkit.global.common.enums.AuthProvider;
import com.walkit.walkit.global.security.oauth.userinfo.*;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class OAuthAttributes {

    private String nameAttributeKey;
    private OAuth2UserInfo oauth2UserInfo;
    private AuthProvider authProvider;

    public static OAuthAttributes of(String registrationId,
                                     String userNameAttributeName,
                                     Map<String, Object> attributes) {

        AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());

        return switch (authProvider) {
            case NAVER -> ofNaver(userNameAttributeName, attributes, authProvider);
            case KAKAO -> ofKakao(userNameAttributeName, attributes, authProvider);
        };
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName,
                                          Map<String, Object> attributes,
                                          AuthProvider authProvider) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new NaverOAuth2UserInfo(attributes))
                .authProvider(authProvider)
                .build();
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName,
                                          Map<String, Object> attributes,
                                          AuthProvider authProvider) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new KakaoOAuth2UserInfo(attributes))
                .authProvider(authProvider)
                .build();
    }

    public User toEntity() {
        return User.builder()
                .email(oauth2UserInfo.getEmail())
                .name(oauth2UserInfo.getName())
                .profileImageUrl(oauth2UserInfo.getProfileImageUrl())
                .authProvider(authProvider)
                .providerId(oauth2UserInfo.getProviderId())
                .role(UserRole.ROLE_USER)
                .build();
    }
}
