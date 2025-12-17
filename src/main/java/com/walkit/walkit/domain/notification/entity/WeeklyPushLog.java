package com.walkit.walkit.domain.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table
public class WeeklyPushLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이번 주 시작일 (월요일)
    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    // 실제 발송 시각
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public WeeklyPushLog(LocalDate weekStart) {
        this.weekStart = weekStart;
        this.sentAt = LocalDateTime.now();
    }
}
