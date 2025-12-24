package com.walkit.walkit.domain.mission.dto;


import com.walkit.walkit.domain.mission.entity.UserWeeklyMission;
import com.walkit.walkit.domain.mission.entity.Mission;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WeeklyMissionResponseDto(
        Long userWeeklyMissionId,
        Long missionId,
        String title,
        String description,
        String category,
        String type,
        String status,
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
                uwm.getCategory().name(),
                m.getType().name(),
                uwm.getStatus().name(),
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
            String category,
            LocalDate weekStart,
            LocalDate weekEnd
    ) {
        return new WeeklyMissionResponseDto(
                null,    // userWeeklyMissionId 없음
                m.getId(),
                m.getTitle(),
                m.getDescription(),
                category,
                m.getType().name(),
                "NONE",             // 제공 안 된 미션 (진행 상태 없음)
                m.getRewardPoints(),
                null,     // assignedConfigJson 없음
                weekStart,
                weekEnd,
                null,          // completedAt 없음
                null                       // failedAt 없음
        );
    }
}
