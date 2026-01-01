package com.walkit.walkit.global.scheduler;

import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupDeletedUsers() {
        log.info("탈퇴 회원 자동 삭제 스케줄러 시작");

        // 6개월 전 날짜 계산
        LocalDateTime threshold = LocalDateTime.now().minusMonths(6);

        // 6개월 이상 지난 탈퇴 회원 조회
        List<User> usersToDelete = userRepository.findDeletedUsersOlderThan(threshold);

        if (usersToDelete.isEmpty()) {
            log.info("삭제할 탈퇴 회원이 없습니다.");
            return;
        }

        log.info("{}명의 탈퇴 회원을 완전 삭제합니다.", usersToDelete.size());

        userRepository.deleteAll(usersToDelete);

        log.info("탈퇴 회원 자동 삭제 완료");
    }
}