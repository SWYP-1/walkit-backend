package com.walkit.walkit.domain.goal.entity;

import com.walkit.walkit.domain.goal.dto.request.RequestGoalDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int targetStepCount;
    private int targetWalkCount;
    private int currentWalkCount;

    @Builder
    public Goal(int targetSteps, int targetWalk, int currentWalks) {
        this.targetStepCount = targetSteps;
        this.targetWalkCount = targetWalk;
        this.currentWalkCount = currentWalks;
    }

    public void update(RequestGoalDto dto) {
        this.targetStepCount = dto.getTargetStepCount();
        this.targetWalkCount = dto.getTargetWalkCount();
    }

   public void plusCurrentWalks() {
       this.currentWalkCount++;
   }

    public void initialize() {
        this.currentWalkCount = 0;
    }
}
