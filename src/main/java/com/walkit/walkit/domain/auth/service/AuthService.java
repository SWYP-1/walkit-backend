package com.walkit.walkit.domain.auth.service;

import com.walkit.walkit.domain.user.repository.RefreshTokenRepository;
import com.walkit.walkit.common.dto.TokenResponse;
import com.walkit.walkit.global.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final JwtService jwtService;

    private final RefreshTokenRepository refreshTokenRepository;

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

    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
