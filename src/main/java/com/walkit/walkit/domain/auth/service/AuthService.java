package com.walkit.walkit.domain.auth.service;

import com.walkit.walkit.domain.user.repository.RefreshTokenRepository;
import com.walkit.walkit.global.common.dto.TokenResponse;
import com.walkit.walkit.global.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        String newAccessToken = jwtService.refreshAccessToken(refreshToken);

        Long userId = jwtService.getUserIdFromToken(refreshToken);
        String newRefreshToken = jwtService.generateRefreshToken(userId);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        Long userId = jwtService.getUserIdFromToken(refreshToken);
        refreshTokenRepository.deleteByUserId(userId);
    }
}
