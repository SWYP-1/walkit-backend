package com.walkit.walkit.domain.mission.repository;

import com.walkit.walkit.domain.mission.entity.MissionCategory;
import com.walkit.walkit.domain.mission.entity.UserWeeklyMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserWeeklyMissionRepository extends JpaRepository<UserWeeklyMission, Long> {


    // missionId 리스트 조회 (유저가 완료한 미션 다음 주 후보에서 제외)
    @Query("""
        select distinct uwm.mission.id
        from UserWeeklyMission uwm
        where uwm.user.id = :userId
          and uwm.status = com.walkit.walkit.domain.mission.entity.MissionStatus.COMPLETED
    """)
    List<Long> findCompletedMissionIds(@Param("userId") Long userId);

    // 주간 마감: 이번 주 IN_PROGRESS -> FAILED
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update UserWeeklyMission uwm
           set uwm.status = com.walkit.walkit.domain.mission.entity.MissionStatus.FAILED,
               uwm.failedAt = CURRENT_TIMESTAMP
         where uwm.weekStart = :weekStart
           and uwm.status = com.walkit.walkit.domain.mission.entity.MissionStatus.IN_PROGRESS
    """)
    int closeWeek(@Param("weekStart") LocalDate weekStart);


    // 이번 주/카테고리 배정 여부 확인
    boolean existsByUser_IdAndWeekStartAndCategory(Long userId, LocalDate weekStart, MissionCategory category);


    // 주간 미션 조회
    @Query("""
    select uwm
    from UserWeeklyMission uwm
    join fetch uwm.mission m
    where uwm.user.id = :userId
      and uwm.weekStart = :weekStart
    order by uwm.category asc, uwm.id asc
""")
    List<UserWeeklyMission> findWeeklyWithMission(
            @Param("userId") Long userId,
            @Param("weekStart") LocalDate weekStart
    );

    @Query("""
    select uwm
    from UserWeeklyMission uwm
    join fetch uwm.user u
    join fetch uwm.mission m
    where uwm.id = :uwmId
""")
    Optional<UserWeeklyMission> findByIdWithUserAndMission(@Param("uwmId") Long uwmId);



}