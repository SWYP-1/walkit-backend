package com.walkit.walkit.domain.auth.controller;

import com.walkit.walkit.domain.auth.dto.AppleTokenLoginRequest;
import com.walkit.walkit.domain.auth.service.AppleService;
import com.walkit.walkit.common.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AppleController {

    private final AppleService appleService;


    @GetMapping("/auth/apple")
    public void loginRequest(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam(value = "redirect_uri", required = false) String redirectUri) throws IOException {
        String appleLoginUrl = appleService.getAppleLoginUrl(redirectUri);
        response.sendRedirect(appleLoginUrl);
    }

    @PostMapping("/api/callback/apple")
    public ResponseEntity<?> callback(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestParam("code") String code,
                                     @RequestParam(value = "state", required = false) String state) throws IOException {

        log.info("Apple callback received - code: {}, state: {}", code != null ? "present" : "null", state);

        // state 파라미터는 redirect_uri를 담고 있음 (웹 클라이언트용)
        boolean isWebClient = (state != null && !state.isEmpty());

        try {
            TokenResponse tokenResponse = appleService.login(code);

            // 로그인 성공
            if (isWebClient) {
                // 웹 클라이언트인 경우 redirect_uri로 리디렉션
                String redirectUrl = appleService.buildSuccessRedirectUrl(state, tokenResponse);
                response.sendRedirect(redirectUrl);
                return null;
            } else {
                // 모바일 앱인 경우 토큰 반환
                return ResponseEntity.ok(tokenResponse);
            }

        } catch (Exception e) {
            log.error("Apple login failed", e);

            if (isWebClient) {
                // 웹 클라이언트인 경우 실패 URL로 리디렉션
                String failureUrl = appleService.buildFailureRedirectUrl(state);
                response.sendRedirect(failureUrl);
                return null;
            } else {
                // 모바일 앱인 경우 에러 응답 반환
                return ResponseEntity.badRequest().body("Apple login failed: " + e.getMessage());
            }
        }
    }


}