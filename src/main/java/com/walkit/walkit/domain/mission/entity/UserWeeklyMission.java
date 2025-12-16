package com.walkit.walkit.domain.mission.entity;

import com.walkit.walkit.domain.BaseTimeEntity;
import com.walkit.walkit.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserWeeklyMission  extends BaseTimeEntity { // 사용자의 미션 기록
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="mission_id", nullable=false)
    private Mission mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private MissionCategory category;

    @Enumerated(EnumType.STRING)
    private MissionStatus status;

    @Column(name = "assigned_config_json", columnDefinition = "TEXT")
    private String assignedConfigJson; // 할당된 설정 (JSON)


    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    private LocalDate weekStart;
    private LocalDate weekEnd;



    public void complete() {
        this.status = MissionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = MissionStatus.FAILED;
    }

    // 도전 가능 여부
    public boolean canAttempt() {
        return this.status == MissionStatus.IN_PROGRESS||
                this.status == MissionStatus.FAILED;
    }

    // 완료 여부
    public boolean isCompleted() {
        return this.status == MissionStatus.COMPLETED;
    }

    // 주차 시작시 초기화 (완료 불가능 했을 경우-실패)
    public void reset() {
        if (this.status != MissionStatus.COMPLETED) {
            this.status = MissionStatus.IN_PROGRESS;
        }
    }

}
