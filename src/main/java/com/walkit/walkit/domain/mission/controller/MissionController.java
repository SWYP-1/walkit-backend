package com.walkit.walkit.domain.mission.controller;

import com.walkit.walkit.domain.mission.dto.WeeklyMissionListResponseDto;
import com.walkit.walkit.domain.mission.dto.WeeklyMissionResponseDto;
import com.walkit.walkit.domain.mission.entity.Mission;
import com.walkit.walkit.domain.mission.entity.MissionCategory;
import com.walkit.walkit.domain.mission.repository.MissionRepository;
import com.walkit.walkit.domain.mission.service.WeeklyMissionClaimService;
import com.walkit.walkit.domain.mission.service.WeeklyMissionService;
import com.walkit.walkit.domain.mission.repository.UserWeeklyMissionRepository;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/missions")
public class MissionController {

    private final WeeklyMissionService weeklyMissionService;
    private final UserWeeklyMissionRepository userWeeklyMissionRepository;
    private final WeeklyMissionClaimService missionClaimService;
    private final MissionRepository missionRepository;

    @GetMapping("/weekly")
    public List<WeeklyMissionResponseDto> myWeekly(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal.getUserId();
        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);

        weeklyMissionService.ensureAssignedForThisWeek(userId);

        return userWeeklyMissionRepository
                .findWeeklyWithMission(userId, weekStart)
                .stream()
                .map(WeeklyMissionResponseDto::fromActive)
                .toList();
    }

    @PostMapping("/weekly/verify/{userwmId}")
    public WeeklyMissionResponseDto verifyWeekly(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userwmId
    ) {
        return missionClaimService.claim(principal.getUserId(),userwmId);
    }

    // 미션 목록 조회 (상단 active 1개 + 아래 나머지 미션)
    @GetMapping("/weekly/list")
    public WeeklyMissionListResponseDto weeklyList(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal.getUserId();
        return weeklyMissionService.getWeeklyMissionList(userId);
    }


    @GetMapping("/completed/monthly")
    public List<WeeklyMissionResponseDto> monthlyCompleted(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return weeklyMissionService.getMonthlyCompletedMissions(principal.getUserId(), year, month);
    }
}
