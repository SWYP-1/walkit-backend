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

    private int targetSteps;
    private int targetWalks;
    private int currentWalks;

    @Builder
    public Goal(int targetSteps, int targetWalk, int currentWalks) {
        this.targetSteps = targetSteps;
        this.targetWalks = targetWalk;
        this.currentWalks = currentWalks;
    }

    public void update(RequestGoalDto dto) {
        this.targetSteps = dto.getTargetSteps();
        this.targetWalks = dto.getTargetWalks();
    }

   public void plusCurrentWalks() {
       this.currentWalks++;
   }

    public void initialize() {
        this.currentWalks = 0;
    }
}
