package com.walkit.walkit.domain.mission.repository;

import com.walkit.walkit.domain.mission.entity.UserMissionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserMissionHistoryRepository extends JpaRepository<UserMissionHistory, Long> {

    @Query("""
select h.completedAt
from UserMissionHistory h
where h.user.id = :userId
  and h.completedAt >= :start
  and h.completedAt < :end
order by h.completedAt asc
""")
    List<LocalDateTime> findCompletedAtInMonth(Long userId, LocalDateTime start, LocalDateTime end);

}
