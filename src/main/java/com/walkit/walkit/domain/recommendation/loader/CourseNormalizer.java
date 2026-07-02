package com.walkit.walkit.domain.recommendation.loader;

import com.walkit.walkit.domain.recommendation.model.Course;
import com.walkit.walkit.domain.recommendation.model.Difficulty;
import com.walkit.walkit.domain.recommendation.model.RawCourseData;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("data-load")
@RequiredArgsConstructor
public class CourseNormalizer {

    private final DurationEstimator durationEstimator;

    // 거리 정보 없을 때 기본값 (서울 평균 산책 코스 ~3km)
    private static final int DEFAULT_DISTANCE_M = 3000;

    public Course normalize(RawCourseData raw) {
        List<double[]> path = raw.getPath() != null ? raw.getPath() : List.of();
        int distanceM = (raw.getDistanceM() != null && raw.getDistanceM() > 0)
                ? raw.getDistanceM() : DEFAULT_DISTANCE_M;

        Course course = Course.builder()
                .externalId(raw.getExternalId())
                .source(raw.getSource())
                .courseName(raw.getCourseName())
                .description(raw.getDescription())
                .distanceM(distanceM)
                .path(path)
                .startLat(coalesce(raw.getStartLat(), firstLat(path)))
                .startLng(coalesce(raw.getStartLng(), firstLng(path)))
                .endLat(coalesce(raw.getEndLat(), lastLat(path)))
                .endLng(coalesce(raw.getEndLng(), lastLng(path)))
                .difficulty(estimateDifficulty(raw.getLevel(), distanceM))
                .build();

        course.setEstimatedMinutes(durationEstimator.estimateMinutes(course));
        return course;
    }

    private Difficulty estimateDifficulty(String level, int distanceM) {
        if (level != null) {
            String lower = level.toLowerCase();
            if (lower.contains("상") || lower.contains("hard") || lower.contains("어려")) return Difficulty.HARD;
            if (lower.contains("하") || lower.contains("easy") || lower.contains("쉬")) return Difficulty.EASY;
            return Difficulty.MEDIUM;
        }
        if (distanceM < 2000) return Difficulty.EASY;
        if (distanceM < 5000) return Difficulty.MEDIUM;
        return Difficulty.HARD;
    }

    private double coalesce(Double apiValue, double fallback) {
        return (apiValue != null) ? apiValue : fallback;
    }

    private Double coalesce(Double apiValue, Double fallback) {
        return (apiValue != null) ? apiValue : fallback;
    }

    private double firstLat(List<double[]> path) {
        return path.isEmpty() ? 37.5665 : path.get(0)[0];
    }

    private double firstLng(List<double[]> path) {
        return path.isEmpty() ? 126.9780 : path.get(0)[1];
    }

    private Double lastLat(List<double[]> path) {
        return path.isEmpty() ? null : path.get(path.size() - 1)[0];
    }

    private Double lastLng(List<double[]> path) {
        return path.isEmpty() ? null : path.get(path.size() - 1)[1];
    }
}
