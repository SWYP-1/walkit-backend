package com.walkit.walkit.domain.notification.service.schedule;

import com.walkit.walkit.domain.mission.service.WeeklyMissionService;
import com.walkit.walkit.domain.notification.service.WalkNotificationService;
import com.walkit.walkit.domain.notification.entity.WeeklyPushLog;
import com.walkit.walkit.domain.notification.repository.WeeklyPushLogRepository;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyMissionPushScheduler {

    private final UserRepository userRepository;
    private final WalkNotificationService walkNotificationService;
    private final WeeklyPushLogRepository weeklyPushRepository;

    // 매주 월요일 오전 9시
    @Scheduled(cron = "0 0 9 ? * MON", zone = "Asia/Seoul")
    @Transactional
    public void notifyWeeklyMissionOpen() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart =
                today.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));


        // 중복 발송 방지
        if (weeklyPushRepository.existsByWeekStart(weekStart)) {
            return; // 이미 발송
        }

        List<User> users = userRepository.findAll();
        for (User user : users) {
            walkNotificationService.notifyNewMission(user);
        }

        weeklyPushRepository.save(new WeeklyPushLog(weekStart));
    }

}

