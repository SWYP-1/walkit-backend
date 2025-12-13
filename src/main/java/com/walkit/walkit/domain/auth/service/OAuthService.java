package com.walkit.walkit.domain.auth.service;

import com.walkit.walkit.domain.auth.dto.KakaoUserInfoResponse;
import com.walkit.walkit.domain.auth.dto.NaverUserInfoResponse;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.enums.UserRole;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.global.common.dto.TokenResponse;
import com.walkit.walkit.global.common.enums.AuthProvider;
import com.walkit.walkit.global.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final WebClient webClient;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String NAVER_USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";

    @Transactional
    public TokenResponse loginWithKakao(String accessToken) {
        KakaoUserInfoResponse kakaoUser = getKakaoUserInfo(accessToken);

        User user = findOrCreateUser(
            AuthProvider.KAKAO,
            String.valueOf(kakaoUser.getId()),
            kakaoUser.getEmail(),
            kakaoUser.getNickname(),
            kakaoUser.getProfileImageUrl()
        );

        String jwtAccessToken = jwtService.generateAccessToken(user.getId());
        String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());

        return TokenResponse.builder()
            .accessToken(jwtAccessToken)
            .refreshToken(jwtRefreshToken)
            .tokenType("Bearer")
            .expiresIn(3600L)
            .build();
    }

    @Transactional
    public TokenResponse loginWithNaver(String accessToken) {
        NaverUserInfoResponse naverUser = getNaverUserInfo(accessToken);

        User user = findOrCreateUser(
            AuthProvider.NAVER,
            naverUser.getId(),
            naverUser.getEmail(),
            naverUser.getName(),
            naverUser.getProfileImage()
        );

        String jwtAccessToken = jwtService.generateAccessToken(user.getId());
        String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());

        return TokenResponse.builder()
            .accessToken(jwtAccessToken)
            .refreshToken(jwtRefreshToken)
            .tokenType("Bearer")
            .expiresIn(3600L)
            .build();
    }

    private KakaoUserInfoResponse getKakaoUserInfo(String accessToken) {
        try {
            return webClient.get()
                .uri(KAKAO_USER_INFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .map(body -> new RuntimeException("Kakao API error: " + response.statusCode() + " - " + body))
                )
                .bodyToMono(KakaoUserInfoResponse.class)
                .block();
        } catch (Exception e) {
            log.error("Failed to get Kakao user info: {}", e.getMessage(), e);
            throw new RuntimeException("카카오 사용자 정보 조회 실패: " + e.getMessage(), e);
        }
    }

    private NaverUserInfoResponse getNaverUserInfo(String accessToken) {
        try {
            return webClient.get()
                .uri(NAVER_USER_INFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .map(body -> new RuntimeException("Naver API error: " + response.statusCode() + " - " + body))
                )
                .bodyToMono(NaverUserInfoResponse.class)
                .block();
        } catch (Exception e) {
            log.error("Failed to get Naver user info: {}", e.getMessage(), e);
            throw new RuntimeException("네이버 사용자 정보 조회 실패: " + e.getMessage(), e);
        }
    }

    private User findOrCreateUser(AuthProvider provider, String providerId,
                                  String email, String name, String profileImageUrl) {
        return userRepository.findByAuthProviderAndProviderId(provider, providerId)
            .orElseGet(() -> {
                User newUser = User.builder()
                    .authProvider(provider)
                    .providerId(providerId)
                    .email(email)
                    .name(name)
                    .profileImageUrl(profileImageUrl)
                    .role(UserRole.ROLE_USER)
                    .build();

                log.info("Creating new user - Provider: {}, ProviderId: {}, Email: {}",
                    provider, providerId, email);

                return userRepository.save(newUser);
            });
    }
}