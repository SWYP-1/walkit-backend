package com.walkit.walkit.domain.walks.repository;


import com.walkit.walkit.domain.walks.entity.Walk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface WalkRepository extends JpaRepository<Walk, Long> {
    Optional<Walk> findByIdAndUser_Id(Long id, Long userId);

    @Query("""
select w from Walk w
left join fetch w.points p
where w.id = :walkId and w.user.id = :userId
""")
    Optional<Walk> findDetailByIdAndUserId(Long walkId, Long userId);  // points까지 같이 조인해서 한 번에 가져옴

}
