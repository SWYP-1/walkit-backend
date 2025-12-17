package com.walkit.walkit.domain.notification.service.schedule;

import com.walkit.walkit.domain.notification.service.WalkNotificationService;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InactiveUserPushScheduler {

    private final UserRepository userRepository;
    private final WalkNotificationService walkNotificationService;

    private static final int BATCH_SIZE = 500;

    // 1시간 마다 실행
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void notifyInactiveUsers48h() {

        log.info("[Inactive48h] scheduler fired");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusHours(48);

        List<User> users = userRepository.findInactive48hTargets(
                threshold,
                PageRequest.of(0, BATCH_SIZE)
        );

        if (users.isEmpty()) {
            log.info("[Inactive48h] fetched=0 (no targets)");
            return;
        }

        List<Long> successIds = new java.util.ArrayList<>();

        for (User user : users) {
            boolean sent = walkNotificationService.notifyInactiveUser(user);
            if (sent) successIds.add(user.getId());
        }

        log.info("[Inactive48h] fetched={}, sent={}", users.size(), successIds.size());

        if (!successIds.isEmpty()) {
            userRepository.markInactive48hNotified(successIds, now);
            log.info("[Inactive48h] marked={}", successIds.size());
        }
    }
}
