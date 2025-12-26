package com.walkit.walkit.global.scheduler;

import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Transactional
@Component
@RequiredArgsConstructor
public class WeeklyCheckAchieveGoalScheduler {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 40 * * * *")
    @Transactional
    public void initializeAchieveGoal() {
        for (User user : userRepository.findAll()) {
            user.initAchieveThisWeekGoal();
        }

    }
}
