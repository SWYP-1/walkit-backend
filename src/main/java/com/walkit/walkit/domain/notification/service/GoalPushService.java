package com.walkit.walkit.domain.notification.service;


import com.walkit.walkit.domain.goal.entity.Goal;
import com.walkit.walkit.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoalPushService {

    private final WalkNotificationService walkNotificationService;

    @Transactional
    public void onWalkCompleted(User user) {
        Goal goal = user.getGoal();
        if (goal == null) return;

        int target = goal.getThisWeekTargetWalkCount();
        if (target <= 0) return;

        //int before = goal.getCurrentWalkCount();

        // 산책 1회 추가
//       goal.plusCurrentWalks();

        int after = goal.getCurrentWalkCount();

        // 100% (완료) 알림: 처음 목표 달성했을 때 1회
        if (!goal.isFullWalkNotified() && after >= target) {
            walkNotificationService.notifyGoalFullWalk(user, after, target);
            goal.markWalkFullNotified();
            return;
        }

        // 50% 알림: 처음 반 넘겼을 때 1회 (완료 전)
        int half = (int) Math.ceil(target * 0.5); // target 홀수일 때, half 반올림
        if (!goal.isHalfWalkNotified()
                && after >= half
                && after < target) {
            walkNotificationService.notifyGoalHalfWalk(user, after, target);
            goal.markWalkHalfNotified();
        }
    }
}
