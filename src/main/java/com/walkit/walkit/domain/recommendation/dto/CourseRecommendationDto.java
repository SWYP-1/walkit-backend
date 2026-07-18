package com.walkit.walkit.domain.recommendation.dto;

import java.util.List;

public record CourseRecommendationDto(
        Long courseId,
        int rank,
        String name,
        String source,
        int distance,
        int estimatedMinutes,
        GeoPoint startPoint,
        GeoPoint endPoint,
        List<String> tags,
        String difficulty,
        double distanceKm,
        double score
) {
    public record GeoPoint(double lat, double lng) {}
}
