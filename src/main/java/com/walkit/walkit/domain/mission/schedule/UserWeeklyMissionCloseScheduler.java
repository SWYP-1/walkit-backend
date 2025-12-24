package com.walkit.walkit.domain.mission.schedule;

import com.walkit.walkit.domain.mission.repository.UserWeeklyMissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserWeeklyMissionCloseScheduler {

    private final UserWeeklyMissionRepository userWeeklyMissionRepository;

    // 일요일 23:59
    @Scheduled(cron = "0 59 23 * * SUN", zone = "Asia/Seoul")
    @Transactional
    public void closeWeeklyMissions() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        int updated = userWeeklyMissionRepository.closeWeek(weekStart);
        log.info("주간 마감 처리 완료: weekStart={}, FAILED 처리 건수={}", weekStart, updated);
    }
}

