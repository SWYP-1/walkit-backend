package com.walkit.walkit.domain.walks.entity;

import com.walkit.walkit.domain.BaseTimeEntity;
import com.walkit.walkit.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "walk_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Walk extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 산책 전 감정
    @Enumerated(EnumType.STRING)
    @Column(name = "preWalkEmotion", nullable = false, length = 20)
    private Emotion preWalkEmotion;

    // 산책 후 감정
    @Enumerated(EnumType.STRING)
    @Column(name = "postWalkEmotion", length = 20)
    private Emotion postWalkEmotion;


    // 산책 기록 텍스트
    @Size(max = 500, message = "텍스트는 최대 500자까지 입력할 수 있습니다.")
    private String note;


    @Column(name = "image_url", length = 255)
    private String imageUrl;

    // 걸음 수
    @Column(name = "stepCount")
    private Integer stepCount;

    // 총 이동 거리
    @Column(name = "totalDistance")
    private Double totalDistance;

    // 산책 시작 시간
    @Column( name = "start_time")
    private Long startTime;

    // 산책 종료 시간
    @Column(name = "end_time")
    private Long endTime;


    // 산책 위치 (경도,위도)
    @Builder.Default
    @OneToMany(mappedBy = "walk", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WalkPoint> points = new ArrayList<>();


    private Double startLatitude;
    private Double startLongitude;

    private Double endLatitude;
    private Double endLongitude;



    public void replacePoints(List<WalkPoint> newPoints) {
        this.points.clear();
        this.points.addAll(newPoints);
    }

    public void updateStartEndFromPoints() {
        if (points == null || points.isEmpty()) {
            startLatitude = startLongitude = endLatitude = endLongitude = null;
            return;
        }
        WalkPoint first = points.get(0);
        WalkPoint last = points.get(points.size() - 1);
        this.startLatitude = first.getLatitude();
        this.startLongitude = first.getLongitude();
        this.endLatitude = last.getLatitude();
        this.endLongitude = last.getLongitude();
    }


    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void complete(
            Long endTime,
            Integer stepCount,
            Double totalDistance,
            String note,
            Emotion postWalkEmotion
    ) {
        this.endTime = endTime;
        this.stepCount = stepCount;
        this.totalDistance = totalDistance;
        this.note = note;
        this.postWalkEmotion = postWalkEmotion;
    }

    public void updateNote(String note) {
        this.note = note;
    }



}
