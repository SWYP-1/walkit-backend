package com.walkit.walkit.domain.goal.service;

import com.walkit.walkit.domain.goal.dto.request.RequestGoalDto;
import com.walkit.walkit.domain.goal.dto.response.ResponseGoalDto;
import com.walkit.walkit.domain.goal.dto.response.ResponseGoalProcessDto;
import com.walkit.walkit.domain.goal.entity.Goal;
import com.walkit.walkit.domain.goal.repository.GoalRepository;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.global.exception.CustomException;
import com.walkit.walkit.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;

    public ResponseGoalDto findGoal(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Goal goal = user.getGoal();

         return ResponseGoalDto.builder().targetSteps(goal.getTargetSteps()).targetWalks(goal.getTargetWalks()).build();
    }

    public void saveGoal(Long userId, RequestGoalDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Goal goal = Goal.builder().targetSteps(dto.getTargetSteps()).targetWalk(dto.getTargetWalks()).build();

        user.updateGoal(goal);
        goalRepository.save(goal);
    }

    public void updateGoal(Long userId, RequestGoalDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Goal beforeGoal = user.getGoal();

        beforeGoal.update(dto);
    }

    public ResponseGoalProcessDto findGoalProcess(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Goal goal = user.getGoal();

        int currentWalks = goal.getCurrentWalks();
        int targetWalks = goal.getTargetWalks();

        double walkProgressPercentage = calculatePercentage(currentWalks, targetWalks);

        return ResponseGoalProcessDto.builder().currentWalks(currentWalks).walkProgressPercentage(walkProgressPercentage).build();
    }

    public void achieveGoal(User user, Goal goal) {
        goal.plusCurrentWalks();

        if (goal.getCurrentWalks() >= goal.getTargetWalks()) {
            // todo 경험치 증가시키기
        }
    }

    private double calculatePercentage(int current, int target) {
        if (target <= 0) return 0.0;
        double percentage = ((double) current / target) * 100.0;
        return Math.min(percentage, 100.0);
    }
}
