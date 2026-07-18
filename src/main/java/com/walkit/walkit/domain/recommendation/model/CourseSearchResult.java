package com.walkit.walkit.domain.recommendation.model;

import java.util.List;

public record CourseSearchResult(
        Long id,
        String externalId,
        String source,
        String courseName,
        String description,
        double startLat,
        double startLng,
        Double endLat,
        Double endLng,
        int distanceM,
        int estimatedMinutes,
        String difficulty,
        List<String> tags,
        List<String> recommendedEmotions,
        double similarityScore
) {}
