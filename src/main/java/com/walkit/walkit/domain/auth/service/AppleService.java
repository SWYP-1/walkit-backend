package com.walkit.walkit.domain.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.walkit.walkit.common.dto.TokenResponse;
import com.walkit.walkit.common.enums.AuthProvider;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.enums.UserRole;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.global.config.AppleProperties;
import com.walkit.walkit.global.security.jwt.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.PrivateKey;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleService {

    private final AppleProperties appleProperties;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    private static final String APPLE_AUTH_URL = "https://appleid.apple.com";

    public String getAppleLoginUrl(String redirectUri) {
        String loginUrl = APPLE_AUTH_URL + "/auth/authorize"
                + "?client_id=" + appleProperties.getClientId()
                + "&redirect_uri=" + appleProperties.getRedirectUrl()
                + "&response_type=code%20id_token"
                + "&scope=name%20email"
                + "&response_mode=form_post";

        // 웹 클라이언트인 경우 state 파라미터에 redirect_uri 저장
        if (redirectUri != null && !redirectUri.isEmpty()) {
            loginUrl = loginUrl + "&state=" + redirectUri;
            log.info("웹 클라이언트의 Apple 로그인 요청 - redirect_uri를 state 파라미터에 추가");
        }

        log.info("Apple 로그인 URL 생성 완료");
        return loginUrl;
    }

    @Transactional
    public TokenResponse login(String code) {
        try {
            // 1. 인가 코드로 Apple access token 및 id_token 발급
            String authTokenResponse = generateAuthToken(code);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(authTokenResponse);

            String accessToken = jsonNode.get("access_token").asText();
            String idToken = jsonNode.get("id_token").asText();

            // 2. ID Token 파싱하여 사용자 정보 추출
            SignedJWT signedJWT = SignedJWT.parse(idToken);
            JWTClaimsSet payload = signedJWT.getJWTClaimsSet();

            String userId = payload.getSubject(); // Apple의 고유 사용자 ID
            String email = payload.getStringClaim("email");

            log.info("Apple 로그인 - userId: {}, email: {}", userId, email);

            // 3. 사용자 조회 또는 생성
            User user = findOrCreateUser(userId, email);

            // 4. JWT 토큰 생성
            String jwtAccessToken = jwtService.generateAccessToken(user.getId());
            String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());

            return TokenResponse.builder()
                    .accessToken(jwtAccessToken)
                    .refreshToken(jwtRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .build();

        } catch (Exception e) {
            log.error("Apple 로그인 실패", e);
            throw new RuntimeException("Apple 로그인에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * Apple SDK에서 받은 identityToken으로 직접 로그인 처리
     * 모바일 앱에서 Apple SDK를 통해 받은 identityToken을 사용
     */
    @Transactional
    public TokenResponse loginWithIdentityToken(String identityToken) {
        try {
            // 1. ID Token 파싱하여 사용자 정보 추출
            SignedJWT signedJWT = SignedJWT.parse(identityToken);
            JWTClaimsSet payload = signedJWT.getJWTClaimsSet();

            String userId = payload.getSubject(); // Apple의 고유 사용자 ID
            String email = payload.getStringClaim("email");

            log.info("Apple identityToken 로그인 - userId: {}, email: {}", userId, email);

            // 2. 사용자 조회 또는 생성
            User user = findOrCreateUser(userId, email);

            // 3. JWT 토큰 생성
            String jwtAccessToken = jwtService.generateAccessToken(user.getId());
            String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());

            return TokenResponse.builder()
                    .accessToken(jwtAccessToken)
                    .refreshToken(jwtRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .build();

        } catch (Exception e) {
            log.error("Apple identityToken 로그인 실패", e);
            throw new RuntimeException("Apple identityToken 로그인에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private String generateAuthToken(String code) throws IOException {
        if (code == null) {
            throw new IllegalArgumentException("인가 코드가 없습니다");
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", appleProperties.getClientId());
        params.add("client_secret", createClientSecretKey());
        params.add("code", code);
        params.add("redirect_uri", appleProperties.getRedirectUrl());

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    APPLE_AUTH_URL + "/auth/token",
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Apple 토큰 발급 실패: {}", e.getResponseBodyAsString());
            throw new IllegalArgumentException("Apple 토큰 발급 실패: " + e.getMessage());
        }
    }

    private String createClientSecretKey() throws IOException {
        Map<String, Object> headerParams = Map.of(
                "kid", appleProperties.getLoginKey(),
                "alg", "ES256"
        );

        return Jwts.builder()
                .setHeader(headerParams)
                .setIssuer(appleProperties.getTeamId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5)) // 5분
                .setAudience(APPLE_AUTH_URL)
                .setSubject(appleProperties.getClientId())
                .signWith(SignatureAlgorithm.ES256, getPrivateKey())
                .compact();
    }

    private PrivateKey getPrivateKey() throws IOException {
        ClassPathResource resource = new ClassPathResource(appleProperties.getKeyPath());
        String privateKey = new String(resource.getInputStream().readAllBytes());

        Reader pemReader = new StringReader(privateKey);
        PEMParser pemParser = new PEMParser(pemReader);
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();

        return converter.getPrivateKey(object);
    }

    private User findOrCreateUser(String providerId, String email) {
        // 1. 삭제된 사용자가 있는지 먼저 확인
        Optional<User> deletedUser = userRepository.findByAuthProviderAndProviderIdAndDeleted(AuthProvider.APPLE, providerId, true);
        if (deletedUser.isPresent()) {
            log.warn("탈퇴한 회원의 로그인 시도 - Provider: APPLE, ProviderId: {}", providerId);
            throw new IllegalStateException("탈퇴한 회원입니다. 재가입은 6개월 후 가능합니다.");
        }

        // 2. 활성 사용자 조회 또는 생성
        return userRepository.findByAuthProviderAndProviderIdAndDeleted(AuthProvider.APPLE, providerId, false)
                .orElseGet(() -> {
                    log.info("신규 Apple 사용자 생성 - providerId: {}, email: {}", providerId, email);

                    User newUser = User.builder()
                            .authProvider(AuthProvider.APPLE)
                            .providerId(providerId)
                            .email(email)
                            .role(UserRole.ROLE_USER)
                            .build();

                    return userRepository.save(newUser);
                });
    }

    public String buildSuccessRedirectUrl(String baseUrl, TokenResponse tokenResponse) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/success")
                .queryParam("token", tokenResponse.getAccessToken())
                .queryParam("refresh_token", tokenResponse.getRefreshToken())
                .build()
                .toUriString();
    }

    public String buildFailureRedirectUrl(String baseUrl) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/fail")
                .build()
                .toUriString();
    }
}