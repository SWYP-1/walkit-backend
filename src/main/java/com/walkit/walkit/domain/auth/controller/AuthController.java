package com.walkit.walkit.domain.auth.controller;

import com.walkit.walkit.domain.auth.dto.OAuthLoginRequest;
import com.walkit.walkit.domain.auth.service.AuthService;
import com.walkit.walkit.domain.auth.service.OAuthService;
import com.walkit.walkit.domain.user.dto.request.RequestRefreshTokenDto;
import com.walkit.walkit.global.common.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final OAuthService oAuthService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RequestRefreshTokenDto request) {
        TokenResponse tokenResponse = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RequestRefreshTokenDto request) {
        authService.logout(request.getRefreshToken());
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

}
