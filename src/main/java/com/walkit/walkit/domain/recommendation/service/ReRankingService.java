package com.walkit.walkit.domain.recommendation.service;

import com.walkit.walkit.domain.recommendation.model.CourseSearchResult;
import com.walkit.walkit.domain.recommendation.model.RankedCourse;
import com.walkit.walkit.domain.walk.entity.Walk;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReRankingService {

    // 벡터 유사도 60% + 지리적 근접도 40%
    private static final double VECTOR_WEIGHT = 0.6;
    private static final double GEO_WEIGHT = 0.4;
    // 사용자가 반경 1km 내에서 산책한 이력이 있으면 +0.1 부스트
    private static final double HISTORY_BOOST = 0.1;
    private static final double HISTORY_RADIUS_KM = 1.0;
    private static final int TOP_RESULTS = 3;

    private final GeoFilterService geoFilter;

    public List<RankedCourse> rerank(List<CourseSearchResult> candidates,
                                      double userLat, double userLng, double radiusKm,
                                      List<Walk> walkHistory) {
        return candidates.stream()
                .map(c -> score(c, userLat, userLng, radiusKm, walkHistory))
                .sorted(Comparator.comparingDouble(RankedCourse::finalScore).reversed())
                .limit(TOP_RESULTS)
                .toList();
    }

    private RankedCourse score(CourseSearchResult course,
                               double userLat, double userLng, double radiusKm,
                               List<Walk> history) {
        double distKm = geoFilter.haversine(userLat, userLng, course.startLat(), course.startLng());
        double geoScore = Math.max(0.0, 1.0 - (distKm / radiusKm));
        double historyBoost = hasNearbyHistory(course, history) ? HISTORY_BOOST : 0.0;
        double finalScore = VECTOR_WEIGHT * course.similarityScore()
                + GEO_WEIGHT * geoScore
                + historyBoost;
        return new RankedCourse(course, distKm, finalScore);
    }

    private boolean hasNearbyHistory(CourseSearchResult course, List<Walk> history) {
        if (history == null || history.isEmpty()) return false;
        return history.stream().anyMatch(w ->
                w.getStartLatitude() != null && w.getStartLongitude() != null
                && geoFilter.haversine(w.getStartLatitude(), w.getStartLongitude(),
                        course.startLat(), course.startLng()) <= HISTORY_RADIUS_KM
        );
    }
}
