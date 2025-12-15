package com.walkit.walkit.global.scheduler;

import com.walkit.walkit.domain.goal.entity.Goal;
import com.walkit.walkit.domain.goal.repository.GoalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyGoalInitializeScheduler {

    private final GoalRepository goalRepository;

    @Scheduled(cron = "0 0 1 * * MON")
    @Transactional
    public void executeWeeklyGoalInitialize() {
        goalRepository.findAll().forEach(Goal::initialize);
    }
}
