package com.walkit.walkit.domain.mission.dto;


import com.walkit.walkit.domain.mission.entity.UserWeeklyMission;
import com.walkit.walkit.domain.mission.entity.Mission;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WeeklyMissionResponse(
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
    public static WeeklyMissionResponse from(UserWeeklyMission uwm) {
        Mission m = uwm.getMission();
        return new WeeklyMissionResponse(
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
}
