package com.walkit.walkit.domain.mission.repository;

import com.walkit.walkit.domain.mission.entity.Mission;
import com.walkit.walkit.domain.mission.entity.MissionCategory;
import com.walkit.walkit.domain.mission.entity.MissionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    List<Mission> findByTypeAndActiveTrue(MissionType type);

    List<Mission> findByCategoryAndActiveTrue(MissionCategory category);

    List<Mission> findByActiveTrue();

}
