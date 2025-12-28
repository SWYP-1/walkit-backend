package com.walkit.walkit.domain.mission.entity;

import com.walkit.walkit.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Mission extends BaseTimeEntity { // 미션 템플릿
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private MissionCategory category;

    @Enumerated(EnumType.STRING)
    private MissionType type;

    @Column(nullable = false)
    private Integer rewardPoints; // 포인트

    /**
     * 미션 설정 템플릿 (JSON)
     * - 걸음수: {"missionSteps": 5000}
     * - 출석: {"requiredDays": 3}
     * - 색깔: {"color":"NAVY"}
     */
    @Column(columnDefinition = "json")
    private String configJson;


    @Column(nullable = false)
    private Boolean active = true; // 미션 활성화 여부


    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}

