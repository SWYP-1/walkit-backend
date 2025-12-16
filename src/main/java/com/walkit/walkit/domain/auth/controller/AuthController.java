package com.walkit.walkit.domain.auth.controller;

import com.walkit.walkit.domain.auth.dto.AppleTokenLoginRequest;
import com.walkit.walkit.domain.auth.dto.OAuthLoginRequest;
import com.walkit.walkit.domain.auth.service.AppleService;
import com.walkit.walkit.domain.auth.service.AuthService;
import com.walkit.walkit.domain.auth.service.OAuthService;
import com.walkit.walkit.domain.user.dto.request.RequestRefreshTokenDto;
import com.walkit.walkit.common.dto.TokenResponse;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final OAuthService oAuthService;
    private final AppleService appleService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RequestRefreshTokenDto request) {
        TokenResponse tokenResponse = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        authService.logout(userPrincipal.getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/kakao")
    public ResponseEntity<TokenResponse> kakaoLogin(@RequestBody OAuthLoginRequest request) {
        TokenResponse tokenResponse = oAuthService.loginWithKakao(request.getAccessToken());
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/naver")
    public ResponseEntity<TokenResponse> naverLogin(@RequestBody OAuthLoginRequest request) {
        TokenResponse tokenResponse = oAuthService.loginWithNaver(request.getAccessToken());
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/apple")
    public ResponseEntity<?> loginWithToken(@RequestBody AppleTokenLoginRequest request) {
        try {
            log.info("Apple identityToken login request received");

            TokenResponse tokenResponse = appleService.loginWithIdentityToken(request.getAccessToken());

            return ResponseEntity.ok(tokenResponse);

        } catch (Exception e) {
            log.error("Apple identityToken login failed", e);
            return ResponseEntity.badRequest().body("Apple login failed: " + e.getMessage());
        }
    }

}
