package com.walkit.walkit.domain.recommendation.dto;

import java.util.List;

public record RecommendationResponse(
        List<CourseRecommendationDto> courses,
        int totalCourses
) {}
