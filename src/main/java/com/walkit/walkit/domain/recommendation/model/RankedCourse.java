package com.walkit.walkit.domain.recommendation.model;

public record RankedCourse(
        CourseSearchResult course,
        double distanceKm,
        double finalScore
) {}
