package com.walkit.walkit.domain.mission.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walkit.walkit.domain.mission.entity.MissionType;
import com.walkit.walkit.domain.mission.entity.UserWeeklyMission;
import com.walkit.walkit.domain.walk.entity.Walk;
import com.walkit.walkit.domain.walk.repository.WalkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.List;
import java.time.LocalDate;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserMissionVerifyService {

    private final WalkRepository walkRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 걸음수 챌린지 검증
    public boolean verifyWeeklyStepChallenge(UserWeeklyMission userMission) {
        if (userMission.getMission().getType() != MissionType.CHALLENGE_STEPS) {
            throw new IllegalArgumentException("걸음수 챌린지가 아닙니다.");
        }

        int missionSteps = getMissionSteps(userMission);


        LocalDateTime start = userMission.getWeekStart().atStartOfDay();
        LocalDateTime end = userMission.getWeekEnd().plusDays(1).atStartOfDay();

        long totalSteps = walkRepository.sumStepsBetween(
                userMission.getUser().getId(),
                start,
                end
        );

        boolean achieved = totalSteps >= missionSteps;

        log.info(
                "주간 걸음수 검증: userId={}, 기간={}~{}, 현재={}, 목표={}, 달성={}",
                userMission.getUser().getId(),
                start, end, totalSteps, missionSteps, achieved
        );

        return achieved;
    }


    // 연속 출석 챌린지 검증
    public boolean verifyWeeklyAttendanceChallenge(UserWeeklyMission userMission) {
        if (userMission.getMission().getType() != MissionType.CHALLENGE_ATTENDANCE) {
            throw new IllegalArgumentException("출석 챌린지가 아닙니다.");
        }

        int requiredDays = getRequiredDays(userMission);

        long startMillis = userMission.getWeekStart()
                .atStartOfDay(ZoneId.of("Asia/Seoul"))
                .toInstant()
                .toEpochMilli();

        long endMillis = userMission.getWeekEnd()
                .plusDays(1) // 일요일 24:00 까지 포함
                .atStartOfDay(ZoneId.of("Asia/Seoul"))
                .toInstant()
                .toEpochMilli();

        // Walk 엔티티 조회
        List<Walk> walks = walkRepository.findWalksBetween(
                userMission.getUser().getId(),
                startMillis,
                endMillis
        );


        // 날짜 추출 및 중복 제거
        List<LocalDate> dates = walks.stream()
                .map(walk -> Instant.ofEpochMilli(walk.getStartTime())
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toLocalDate())
                .distinct()
                .sorted()
                .toList();


        // 연속 일수 계산
        int maxStreak = 0;
        int streak = 0;
        LocalDate prev = null;

        for (LocalDate d : dates) {
            if (prev == null) {
                streak = 1;
            } else if (d.equals(prev.plusDays(1))) {
                streak++;
            } else {
                streak = 1;
            }
            maxStreak = Math.max(maxStreak, streak);
            prev = d;
        }

        boolean achieved = maxStreak >= requiredDays;

        log.info("attendance range millis: startMillis={}, endMillis={}", startMillis, endMillis);
        log.info("attendance range kst: {} ~ {}",
                Instant.ofEpochMilli(startMillis).atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime(),
                Instant.ofEpochMilli(endMillis).atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
        );

        log.info("출석 검증: userId={}, dates={}, maxStreak={}, requiredDays={}, achieved={}",
                userMission.getUser().getId(), dates, maxStreak, requiredDays, achieved);

        return achieved;
    }


    // AssignedConfigJson 연속 일자 추출
    private int getRequiredDays(UserWeeklyMission userMission) {
        try {
            Map<String, Object> config =
                    objectMapper.readValue(userMission.getAssignedConfigJson(), Map.class);

            Object raw = config.get("requiredDays");
            if (raw == null) {
                throw new IllegalStateException("assignedConfigJson에 requiredDays 없음");
            }
            return ((Number) raw).intValue();
        } catch (Exception e) {
            throw new RuntimeException("설정 파싱 실패", e);
        }
    }


    // AssignedConfigJson 걸음 수 추출
    private int getMissionSteps(UserWeeklyMission userMission) {
        try {
            Map<String, Object> config =
                    objectMapper.readValue(userMission.getAssignedConfigJson(), Map.class);

            Object raw = config.get("missionSteps");
            if (raw == null) {
                throw new IllegalStateException("assignedConfigJson에 missionSteps 없음");
            }

            return ((Number) raw).intValue();
        } catch (Exception e) {
            throw new RuntimeException("설정 파싱 실패", e);
        }
    }
}
