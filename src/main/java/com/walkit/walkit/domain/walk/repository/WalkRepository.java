package com.walkit.walkit.domain.walk.repository;


import com.walkit.walkit.domain.walk.entity.Walk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface WalkRepository extends JpaRepository<Walk, Long> {
    Optional<Walk> findByIdAndUser_Id(Long walkId, Long userId);

    @Query("""
select w from Walk w
left join fetch w.points p
where w.id = :walkId and w.user.id = :userId
""")
    Optional<Walk> findDetailByIdAndUserId(Long walkId, Long userId);  // points까지 같이 조인해서 한 번에 가져옴

    // 주간 걸음수 합
    @Query("""
        select coalesce(sum(w.stepCount), 0)
        from Walk w
        where w.user.id = :userId
          and w.createdDate >= :start
          and w.createdDate < :end
    """)
    long sumStepsBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // startTime(millis) 기준으로 기간 내 산책 시작 '날짜' 목록
    @Query("SELECT w FROM Walk w " +
            "WHERE w.user.id = :userId " +
            "AND w.startTime >= :startMillis " +
            "AND w.startTime < :endMillis " +
            "ORDER BY w.startTime")
    List<Walk> findWalksBetween(
            @Param("userId") Long userId,
            @Param("startMillis") Long startMillis,
            @Param("endMillis") Long endMillis
    );



    // 일간 산책 단건 조회
    Optional<Walk> findFirstByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeDesc(
            Long userId, Long dayEnd, Long dayStart
    );

}
