package com.walkit.walkit.domain.goal.service;

import com.walkit.walkit.common.image.entity.CharacterWearImage;
import com.walkit.walkit.common.image.repository.CharacterWearImageRepository;
import com.walkit.walkit.domain.character.entity.Character;
import com.walkit.walkit.domain.character.entity.CharacterWear;
import com.walkit.walkit.domain.character.enums.Grade;
import com.walkit.walkit.domain.item.enums.ItemName;
import com.walkit.walkit.domain.item.enums.Position;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GoalService {

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final CharacterWearImageRepository characterWearImageRepository;

    public ResponseGoalDto findGoal(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Goal goal = user.getGoal();

         return ResponseGoalDto.builder().targetStepCount(goal.getTargetStepCount()).targetWalkCount(goal.getTargetWalkCount()).build();
    }

    public void saveGoal(Long userId, RequestGoalDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Goal goal = Goal.builder().targetStepCount(dto.getTargetStepCount()).targetWalkCount(dto.getTargetWalkCount()).build();

        user.updateGoal(goal);
        goalRepository.save(goal);
    }

    public void updateGoal(Long userId, RequestGoalDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Goal beforeGoal = user.getGoal();

        if (beforeGoal.getModifiedDate() != null) {
            long daySinceLastUpdate = ChronoUnit.DAYS.between(
                    beforeGoal.getModifiedDate(),
                    LocalDateTime.now()
            );

            if (daySinceLastUpdate < 30) {
                throw new CustomException(ErrorCode.GOAL_UPDATE_NOT_ALLOWED);
            }
        }

        beforeGoal.update(dto);
    }

    public ResponseGoalProcessDto findGoalProcess(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Goal goal = user.getGoal();

        int currentWalks = goal.getCurrentWalkCount();
        int targetWalks = goal.getTargetWalkCount();

        String walkProgressPercentage = calculatePercentage(currentWalks, targetWalks);

        return ResponseGoalProcessDto.builder().currentWalkCount(currentWalks).walkProgressPercentage(walkProgressPercentage).build();
    }

    public void checkAchieveGoal(User user, int stepCount) {
        Goal goal = user.getGoal();

        if (goal != null) {
            checkAchieveTargetStepCount(stepCount, goal);
            checkAchieveGoal(user, goal);
        }
    }

    private void checkAchieveGoal(User user, Goal goal) {

        log.info("checkAchieveGoal, currentWalkCount: {}, targetWalkCount: {}, isAchieveThisWeekGoal, {}", goal.getCurrentWalkCount(), goal.getTargetWalkCount(), user.isAchieveThisWeekGoal());

        if (goal.getCurrentWalkCount() >= goal.getTargetWalkCount() && !user.isAchieveThisWeekGoal()) {

            log.info("checkAchieveGoal");

            Character character = user.getCharacter();
            int level = character.getLevel();

            log.info("level: " + level);

            if (level >= 5) {
                log.info("checkAchieveGoal");
                achieveConsecutiveWeeksGoal(user);
            } else {
                achieveTotalWeeksGoal(user);
            }

            user.achieveThisWeekGoal();
        }
    }

    private void achieveConsecutiveWeeksGoal(User user) {
        user.plusAchieveConsecutiveWeeks();
        user.plusAchieveTotalWeeks();

        int achieveGoalConsecutiveWeeks = user.getAchieveGoalConsecutiveWeeks();
        Character character = user.getCharacter();

        log.info("achieveGoalConsecutiveWeeks: " + achieveGoalConsecutiveWeeks);

        if (achieveGoalConsecutiveWeeks >= 2 && achieveGoalConsecutiveWeeks <= 10) {
            levelUpByConsecutiveWeeks(user, achieveGoalConsecutiveWeeks, character);
            user.initAchieveConsecutiveWeeks();
        }
    }

    private void achieveTotalWeeksGoal(User user) {
        user.plusAchieveTotalWeeks();

        int achieveGoalTotalWeeks = user.getAchieveGoalTotalWeeks();
        Character character = user.getCharacter();

        if (achieveGoalTotalWeeks >= 1 && achieveGoalTotalWeeks <= 10) {
            levelUpByTotalWeeks(achieveGoalTotalWeeks, character);
        }
    }

    private void levelUpByConsecutiveWeeks(User user, int achieveGoalConsecutiveWeeks, Character character) {
        if (achieveGoalConsecutiveWeeks == 2) {
            character.updateLevel(6);
            user.initAchieveConsecutiveWeeks();
        } else if (achieveGoalConsecutiveWeeks == 4) {
            character.updateLevel(7);
            user.initAchieveConsecutiveWeeks();
        } else if (achieveGoalConsecutiveWeeks == 6) {
            character.updateLevel(8);
            user.initAchieveConsecutiveWeeks();
        } else if (achieveGoalConsecutiveWeeks == 8) {
            character.updateLevel(9);
            user.initAchieveConsecutiveWeeks();
        } else if (achieveGoalConsecutiveWeeks == 10) {
            character.updateLevel(10);
            user.initAchieveConsecutiveWeeks();
        }

        changeCharacterWearByGradeUp(character);
    }

    private void changeCharacterWearByGradeUp(Character character) {
        boolean gradeUp = character.gradeUp();

        if (gradeUp) {
            Grade grade = character.getGrade();
            for (CharacterWear characterWear : character.getCharacterWears()) {
                Position position = characterWear.getItem().getPosition();
                ItemName itemName = characterWear.getItem().getItemName();

                CharacterWearImage characterWearImage = characterWearImageRepository.findByPositionAndGradeAndItemName(position, grade, itemName);

                character.updateImage(characterWearImage);
            }

        }
    }

    private void levelUpByTotalWeeks(int achieveGoalTotalWeeks, Character character) {
        if (achieveGoalTotalWeeks == 1) {
            character.updateLevel(1);
        } else if (achieveGoalTotalWeeks == 4) {
            character.updateLevel(2);
        } else if (achieveGoalTotalWeeks == 6) {
            character.updateLevel(3);
        } else if (achieveGoalTotalWeeks == 8) {
            character.updateLevel(4);
        } else if (achieveGoalTotalWeeks == 10) {
            character.updateLevel(5);
        }

        changeCharacterWearByGradeUp(character);
    }

    private static void checkAchieveTargetStepCount(int stepCount, Goal goal) {
        log.info("checkAchieveTargetStepCount: stepCount = " + stepCount);
        if (goal.getTargetStepCount() <= stepCount) {
            log.info("plushCurrenetWalks");
            goal.plusCurrentWalks();
        }
    }

    private String calculatePercentage(int current, int target) {
        if (target <= 0) {
            return "0.0";
        }

        double percentage = ((double) current / target) * 100.0;
        double limitedPercentage = Math.min(percentage, 100.0);

        return String.format("%.1f", limitedPercentage);
    }
}
