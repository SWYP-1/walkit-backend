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
    ); // 주간 걸음수 합

    @Query("""
    select distinct function('date', w.createdDate)
    from Walk w
    where w.user.id = :userId
      and w.createdDate >= :start
      and w.createdDate < :end
    order by function('date', w.createdDate)
""")
    List<Date> findDistinctWalkDatesBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );



}
