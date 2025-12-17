package com.walkit.walkit.domain.notification.repository;

import com.walkit.walkit.domain.notification.entity.WeeklyPushLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface WeeklyPushLogRepository extends JpaRepository<WeeklyPushLog, Long> {

    // 해당 주차에 이미 공통 알림을 보냈는지 확인
    boolean existsByWeekStart(LocalDate weekStart);
}