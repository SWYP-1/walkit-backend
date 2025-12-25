package com.walkit.walkit.domain.mission.dto;


import com.walkit.walkit.domain.mission.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WeeklyMissionResponseDto(
        Long userWeeklyMissionId,
        Long missionId,
        String title,
        String description,
        MissionCategory category,
        MissionType type,
        MissionStatus status,
        Integer rewardPoints,
        String assignedConfigJson,
        LocalDate weekStart,
        LocalDate weekEnd,
        LocalDateTime completedAt,
        LocalDateTime failedAt
) {
    // 이번 주 실제 배정된 미션
    public static WeeklyMissionResponseDto fromActive(UserWeeklyMission uwm) {
        Mission m = uwm.getMission();
        return new WeeklyMissionResponseDto(
                uwm.getId(),
                m.getId(),
                m.getTitle(),
                m.getDescription(),
                uwm.getCategory(),
                m.getType(),
                uwm.getStatus(),
                m.getRewardPoints(),
                uwm.getAssignedConfigJson(),
                uwm.getWeekStart(),
                uwm.getWeekEnd(),
                uwm.getCompletedAt(),
                uwm.getFailedAt()
        );
    }

    // 이번 주 제공 안 된 미션 (mission 기반, 표시용)
    public static WeeklyMissionResponseDto fromUnActive(
            Mission m,
            MissionCategory category,
            LocalDate weekStart,
            LocalDate weekEnd
    ) {
        return new WeeklyMissionResponseDto(
                null,    // userWeeklyMissionId 없음
                m.getId(),
                m.getTitle(),
                m.getDescription(),
                category,
                m.getType(),
                null,               // 제공 안 된 미션 (진행 상태 없음)
                m.getRewardPoints(),
                null,     // assignedConfigJson 없음
                weekStart,
                weekEnd,
                null,          // completedAt 없음
                null                       // failedAt 없음
        );
    }
}
