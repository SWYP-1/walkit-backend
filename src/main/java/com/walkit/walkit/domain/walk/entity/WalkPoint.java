package com.walkit.walkit.domain.walk.entity;


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

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Long recordedAt;  // GPS timestampMillis

    public static WalkPoint of(Walk walk, double lat, double lng, long recordedAt) {
        WalkPoint p = new WalkPoint();
        p.walk = walk;
        p.latitude = lat;
        p.longitude = lng;
        p.recordedAt = recordedAt;
        return p;
    }
}

