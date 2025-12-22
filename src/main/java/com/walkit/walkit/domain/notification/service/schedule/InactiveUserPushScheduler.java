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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusHours(48);

        int totalFetched = 0;
        int totalSent = 0;
        int totalMarked = 0;

        while (true) {
            List<User> users = userRepository.findInactive48hTargets(
                    threshold, PageRequest.of(0, BATCH_SIZE)
            );
            if (users.isEmpty()) break;

            totalFetched += users.size();

            List<Long> successIds = new java.util.ArrayList<>();
            for (User user : users) {
                boolean sent = walkNotificationService.notifyInactiveUser(user);
                if (sent) successIds.add(user.getId());
            }

            totalSent += successIds.size();

            if (!successIds.isEmpty()) {
                userRepository.markInactive48hNotified(successIds, now);
                totalMarked += successIds.size();
            } else {
                break;
            }
        }

        log.info("[Inactive48h] fetchedTotal={}, sentTotal={}, markedTotal={}",
                totalFetched, totalSent, totalMarked);
    }
}
