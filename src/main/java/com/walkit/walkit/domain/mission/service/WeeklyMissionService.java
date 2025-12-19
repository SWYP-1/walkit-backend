package com.walkit.walkit.domain.mission.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.walkit.walkit.domain.mission.entity.*;
import com.walkit.walkit.domain.mission.repository.MissionRepository;
import com.walkit.walkit.domain.mission.repository.UserWeeklyMissionRepository;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyMissionService {

    private final MissionRepository missionRepository;
    private final UserWeeklyMissionRepository userWeeklyMissionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final LocalDate BASE_WEEK_START = LocalDate.of(2025, 1, 6);
    private static final MissionColor[] COLORS = MissionColor.values();

    @Transactional
    public void ensureAssignedForThisWeek(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        LocalDate weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        // 챌린지 1개
        if (!userWeeklyMissionRepository.existsByUser_IdAndWeekStartAndCategory(userId, weekStart, MissionCategory.CHALLENGE)) {
            Mission challenge = pickRandomChallengeExcludingCompleted(userId);
            userWeeklyMissionRepository.save(UserWeeklyMission.builder()
                    .user(user)
                    .mission(challenge)
                    .category(MissionCategory.CHALLENGE)
                    .status(MissionStatus.IN_PROGRESS)
                    .assignedConfigJson(challenge.getConfigJson())
                    .weekStart(weekStart)
                    .weekEnd(weekEnd)
                    .build());
        }

        /*// 사진 1개 - 색상 랜덤
        if (!userWeeklyMissionRepository.existsByUser_IdAndWeekStartAndCategory(userId, weekStart, MissionCategory.PHOTO)) {

            List<Mission> photoMissions = missionRepository.findByTypeAndActiveTrue(MissionType.PHOTO_COLOR);
            if (photoMissions.isEmpty()) {
                throw new IllegalStateException("PHOTO_COLOR 미션이 없습니다.");
            }

            // 사진 미션 템플릿 랜덤
            Mission photo = photoMissions.get(ThreadLocalRandom.current().nextInt(photoMissions.size()));

            String assignedConfig = photo.getConfigJson();


            userWeeklyMissionRepository.save(UserWeeklyMission.builder()
                    .user(user)
                    .mission(photo)
                    .category(MissionCategory.PHOTO)
                    .status(MissionStatus.IN_PROGRESS)
                    .assignedConfigJson(assignedConfig)
                    .weekStart(weekStart)
                    .weekEnd(weekEnd)
                    .build());
        }*/
    }

    private Mission pickRandomChallengeExcludingCompleted(Long userId) {
        List<Mission> all = missionRepository.findByCategoryAndActiveTrue(MissionCategory.CHALLENGE);
        if (all.isEmpty()) throw new IllegalStateException("활성 챌린지 미션 없음");

        List<Long> completedIds = userWeeklyMissionRepository.findCompletedMissionIds(userId);

        List<Mission> candidates = all.stream()
                .filter(this::isWeeklyValid)
                .filter(m -> !completedIds.contains(m.getId()))
                .toList();

        List<Mission> pool = candidates.isEmpty()
                ? all.stream().filter(this::isWeeklyValid).toList()
                : candidates;

        if (pool.isEmpty()) {
            throw new IllegalStateException("챌린지 후보 풀이 비었습니다. (weeklyValid도 없음)");
        }

        return pool.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(pool.size()));
    }


    private boolean isWeeklyValid(Mission m) {
        if (m.getType() == MissionType.CHALLENGE_STEPS) return true;

        if (m.getType() == MissionType.CHALLENGE_ATTENDANCE) {
            Integer requiredDays = readIntFromConfig(m.getConfigJson(), "requiredDays");
            return requiredDays != null && requiredDays <= 7;
        }
        return true;
    }

    private Integer readIntFromConfig(String json, String key) {
        try {
            if (json == null) return null;
            var node = objectMapper.readTree(json);
            var v = node.get(key);
            return (v != null && v.isInt()) ? v.asInt() : null;
        } catch (Exception e) {
            log.warn("configJson 파싱 실패: {}", json, e);
            return null;
        }
    }

  /*  private MissionColor pickColorForWeek(LocalDate weekStart) {
        long weeks = java.time.temporal.ChronoUnit.WEEKS.between(BASE_WEEK_START, weekStart);
        int idx = (int) Math.floorMod(weeks, COLORS.length);
        return COLORS[idx];
    }*/

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }
}
