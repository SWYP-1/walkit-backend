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

    private boolean halfWalkNotified;     // 50% 알림 보냈는지
    private boolean fullWalkNotified;     // 100% 알림 보냈는지

    @Builder
    public Goal(int targetStepCount, int targetWalkCount, int currentWalkCount) {
        this.targetStepCount = targetStepCount;
        this.targetWalkCount = targetWalkCount;
        this.currentWalkCount = currentWalkCount;
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
        this.halfWalkNotified = false;
        this.fullWalkNotified = false;
    }

    public boolean isWalkHalfNotified() { return halfWalkNotified; }
    public boolean isWalkFullNotified() { return fullWalkNotified; }

    public void markWalkHalfNotified() { this.halfWalkNotified = true; }
    public void markWalkFullNotified() { this.fullWalkNotified = true; }

}
