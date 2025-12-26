package com.walkit.walkit.domain.mission.service;

import com.walkit.walkit.domain.mission.dto.WeeklyMissionResponseDto;
import com.walkit.walkit.domain.mission.entity.UserMissionHistory;
import com.walkit.walkit.domain.mission.entity.UserWeeklyMission;
import com.walkit.walkit.domain.mission.repository.UserMissionHistoryRepository;
import com.walkit.walkit.domain.mission.repository.UserWeeklyMissionRepository;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.global.exception.CustomException;
import com.walkit.walkit.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WeeklyMissionClaimService {

    private final UserWeeklyMissionRepository userWeeklyMissionRepository;
    private final UserMissionVerifyService missionVerifyService;
    private final UserRepository userRepository;
    private final UserMissionHistoryRepository userMissionHistoryRepository;


    @Transactional
    public WeeklyMissionResponseDto claim(Long loginUserId, Long uwmId) {

        UserWeeklyMission uwm = userWeeklyMissionRepository.findByIdWithUserAndMission(uwmId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        // 권한 체크 (가장 중요)
        if (!uwm.getUser().getId().equals(loginUserId)) {
            throw new CustomException(ErrorCode.MISSION_NOT_OWNED);
        }

        // 이미 완료면 중복 지급 방지
        if (uwm.isCompleted()) {
            throw new CustomException(ErrorCode.MISSION_ALREADY_COMPLETED);
        }


        // 미션 타입별 검증
        boolean achieved = switch (uwm.getMission().getType()) {
            case CHALLENGE_STEPS -> missionVerifyService.verifyWeeklyStepChallenge(uwm);
            case CHALLENGE_ATTENDANCE -> missionVerifyService.verifyWeeklyAttendanceChallenge(uwm);
            //case PHOTO_COLOR -> missionVerifyService.verifyPhotoColor(uwm);
            default ->  throw new CustomException(ErrorCode.MISSION_TYPE_NOT_SUPPORTED);
        };

        if (!achieved) {
            throw new CustomException(ErrorCode.MISSION_NOT_ACHIEVED);
        }

        // 완료 처리
        LocalDateTime now = LocalDateTime.now();
        uwm.complete(now);

        // 미션 완료 히스토리 저장
        userMissionHistoryRepository.save(
                UserMissionHistory.from(uwm, now)
        );

        // 포인트 지급
        User user = uwm.getUser();
        user.addPoints(uwm.getMission().getRewardPoints());
        userRepository.save(user);


        return WeeklyMissionResponseDto.fromActive(uwm);
    }
}
