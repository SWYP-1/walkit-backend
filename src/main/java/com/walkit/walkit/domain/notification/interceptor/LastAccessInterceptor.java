package com.walkit.walkit.domain.notification.interceptor;

import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.domain.user.service.UserService;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LastAccessInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    private final UserService userService;

    // 10분 쿨다운
    private static final long COOLDOWN_MINUTES = 10;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return true;
        }

        Object principalObj = auth.getPrincipal();
        if (!(principalObj instanceof UserPrincipal principal)) {
            return true;
        }

        Long userId = principal.getUserId();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime last = userService.findLastAccessAt(userId);
        if (last == null || last.isBefore(now.minusMinutes(COOLDOWN_MINUTES))) {
            userService.updateLastAccessAt(userId, now);
        }

        return true;
    }
}


