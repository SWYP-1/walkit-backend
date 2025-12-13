package com.walkit.walkit.domain.walks.entity;

import com.walkit.walkit.domain.BaseTimeEntity;
import com.walkit.walkit.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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


    @Enumerated(EnumType.STRING)
    @Column(name = "emotion", nullable = false, length = 20)
    private Emotion emotion;


    @Column(name = "text", length = 100)
    private String text;


    @Column(name = "image_url", length = 255)
    private String imageUrl;

    // 산책한 시각
    @Column(name = "walked_at", nullable = false)
    private LocalDate walkedAt;

    @OneToMany(mappedBy = "walk", cascade = CascadeType.ALL, orphanRemoval = true)
    //@OrderBy("seq asc")
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

    public static Walk create(User user, Emotion emotion, String text, LocalDate walkedAt, String imageUrl) {
        Walk w = new Walk();
        w.user = user;
        w.emotion = emotion;
        w.text = text;
        w.walkedAt = walkedAt;
        w.imageUrl = imageUrl;
        return w;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }




}
