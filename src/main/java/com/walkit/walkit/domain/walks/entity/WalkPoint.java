package com.walkit.walkit.domain.walks.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalkPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "walk_id", nullable = false)
    private Walk walk;

    // 순서 (지도)
    @Column(nullable = false)
    private Integer seq;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    // 측정 시각
    @Column(nullable = false)
    private java.time.Instant recordedAt;

    public static WalkPoint of(Walk walk, int seq, double lat, double lng, java.time.Instant recordedAt) {
        WalkPoint p = new WalkPoint();
        p.walk = walk;
        p.seq = seq;
        p.latitude = lat;
        p.longitude = lng;
        p.recordedAt = recordedAt;
        return p;
    }
}

