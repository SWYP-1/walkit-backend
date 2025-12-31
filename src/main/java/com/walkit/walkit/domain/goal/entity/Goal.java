package com.walkit.walkit.domain.goal.entity;

import com.walkit.walkit.domain.BaseTimeEntity;
import com.walkit.walkit.domain.goal.dto.request.RequestGoalDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Goal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int thisWeekTargetStepCount;
    private int thisWeekTargetWalkCount;

    private Integer nextWeekTargetStepCount;
    private Integer nextWeekTargetWalkCount;


    private int currentWalkCount;

    private boolean halfWalkNotified;     // 50% 알림 보냈는지
    private boolean fullWalkNotified;     // 100% 알림 보냈는지

    private LocalDateTime updatedDate;

    @Builder
    public Goal(int thisWeekTargetStepCount, int thisWeekTargetWalkCount, int currentWalkCount) {
        this.thisWeekTargetStepCount = thisWeekTargetStepCount;
        this.thisWeekTargetWalkCount = thisWeekTargetWalkCount;
        this.currentWalkCount = currentWalkCount;
    }

    public void update(RequestGoalDto dto) {
        this.thisWeekTargetStepCount = dto.getTargetStepCount();
        this.thisWeekTargetWalkCount = dto.getTargetWalkCount();
    }

    public void updateNextTargetStepCount(int stepCount) {
        this.nextWeekTargetStepCount = stepCount;
    }

    public void updateNextTargetWalkCount(int walkCount) {
        this.nextWeekTargetWalkCount = walkCount;
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

    public void applyNextGoal() {
        if (this.nextWeekTargetStepCount != null) {
            this.thisWeekTargetStepCount = this.nextWeekTargetStepCount;
            this.nextWeekTargetStepCount = null;
        }
        if (this.nextWeekTargetWalkCount != null) {
            this.thisWeekTargetWalkCount = this.nextWeekTargetWalkCount;
            this.nextWeekTargetWalkCount = null;
        }
    }

    public void setUpdatedDate() {
        this.updatedDate = LocalDateTime.now();
    }
}
