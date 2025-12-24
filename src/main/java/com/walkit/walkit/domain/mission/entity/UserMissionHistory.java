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
@Table(
        name = "user_mission_history",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_week", columnNames = {"user_id", "week_start"})
        },
        indexes = {
                @Index(name = "idx_user_completed_at", columnList = "user_id, completed_at")
        }
)
public class UserMissionHistory extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="mission_id", nullable=false)
    private Mission mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private MissionCategory category;

    @Column(name="completed_at", nullable=false)
    private LocalDateTime completedAt;

    @Column(name="week_start", nullable=false)
    private LocalDate weekStart;

    @Column(name="week_end", nullable=false)
    private LocalDate weekEnd;


    public static UserMissionHistory from(UserWeeklyMission uwm, LocalDateTime now) {
        return UserMissionHistory.builder()
                .user(uwm.getUser())
                .mission(uwm.getMission())
                .category(uwm.getCategory())
                .completedAt(now)
                .weekStart(uwm.getWeekStart())
                .weekEnd(uwm.getWeekEnd())
                .build();
    }
}
