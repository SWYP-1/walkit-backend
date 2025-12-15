package com.walkit.walkit.domain.goal.repository;

import com.walkit.walkit.domain.goal.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, Long> {
}
