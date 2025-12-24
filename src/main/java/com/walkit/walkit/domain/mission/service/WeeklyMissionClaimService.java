package com.walkit.walkit.domain.mission.service;

import com.walkit.walkit.domain.mission.dto.WeeklyMissionResponseDto;
import com.walkit.walkit.domain.mission.entity.UserMissionHistory;
import com.walkit.walkit.domain.mission.entity.UserWeeklyMission;
import com.walkit.walkit.domain.mission.repository.UserMissionHistoryRepository;
import com.walkit.walkit.domain.mission.repository.UserWeeklyMissionRepository;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
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
                .orElseThrow(() -> new IllegalArgumentException("미션 없음"));

        // 권한 체크 (가장 중요)
        if (!uwm.getUser().getId().equals(loginUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("본인 미션이 아닙니다.");
        }

        // 이미 완료면 중복 지급 방지
        if (uwm.isCompleted()) {
            throw new IllegalStateException("이미 보상을 수령했습니다.");
        }

        // 도전 가능 여부 확인
        if (!uwm.canAttempt()) {
            throw new IllegalStateException("현재 상태에서는 보상 수령이 불가합니다.");
        }

        // 미션 타입별 검증
        boolean achieved = switch (uwm.getMission().getType()) {
            case CHALLENGE_STEPS -> missionVerifyService.verifyWeeklyStepChallenge(uwm);
            case CHALLENGE_ATTENDANCE -> missionVerifyService.verifyWeeklyAttendanceChallenge(uwm);
            //case PHOTO_COLOR -> missionVerifyService.verifyPhotoColor(uwm);
            default -> throw new IllegalStateException("지원하지 않는 미션 타입");
        };

        if (!achieved) {
            throw new IllegalStateException("아직 미션 목표를 달성하지 못했습니다.");
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
