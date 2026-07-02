package com.walkit.walkit.domain.recommendation.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RawCourseData {
    private String externalId;
    private String source;
    private String courseName;
    private String description;
    private Integer distanceM;
    private Double startLat;
    private Double startLng;
    private Double endLat;
    private Double endLng;
    private String level;
    private List<double[]> path;
    private String rawJson;
}
