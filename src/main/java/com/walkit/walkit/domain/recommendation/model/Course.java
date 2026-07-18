package com.walkit.walkit.domain.recommendation.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Course {
    private String externalId;
    private String source;
    private String courseName;
    private String description;
    private int distanceM;
    private double startLat;
    private double startLng;
    private Double endLat;
    private Double endLng;
    private List<double[]> path;
    private Difficulty difficulty;
    private int estimatedMinutes;
    // API가 소요시간을 제공한 경우 저장 (DurationEstimator에서 우선 사용)
    private Integer providedEstimatedMinutes;

    public boolean hasProvidedEstimatedTime() {
        return providedEstimatedMinutes != null && providedEstimatedMinutes > 0;
    }
}
