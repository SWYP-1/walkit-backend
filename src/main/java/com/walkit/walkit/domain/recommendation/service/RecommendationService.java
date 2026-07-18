package com.walkit.walkit.domain.recommendation.service;

import com.walkit.walkit.domain.recommendation.dto.CourseRecommendationDto;
import com.walkit.walkit.domain.recommendation.dto.RecommendationRequest;
import com.walkit.walkit.domain.recommendation.dto.RecommendationResponse;
import com.walkit.walkit.domain.recommendation.model.CourseSearchResult;
import com.walkit.walkit.domain.recommendation.model.RankedCourse;
import com.walkit.walkit.domain.walk.entity.Walk;
import com.walkit.walkit.domain.walk.repository.WalkRepository;
import com.walkit.walkit.global.exception.CustomException;
import com.walkit.walkit.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final EmbeddingService embeddingService;
    private final VectorSearchService vectorSearch;
    private final GeoFilterService geoFilter;
    private final ReRankingService reRanking;
    private final WalkRepository walkRepository;

    public RecommendationResponse recommend(RecommendationRequest req, Long userId) {
        float[] queryEmbedding = embeddingService.embedQuery(req.getEmotion(), req.getDesiredMinutes());

        List<CourseSearchResult> candidates = vectorSearch.search(queryEmbedding, req.getDesiredMinutes());

        double radiusKm = req.getRadiusKm() != null ? req.getRadiusKm() : 5.0;
        List<CourseSearchResult> nearby = geoFilter.filterByRadius(
                candidates, req.getLatitude(), req.getLongitude(), radiusKm);
        log.debug("지오 필터 후: {}개 (반경 {}km)", nearby.size(), radiusKm);

        if (nearby.isEmpty()) {
            throw new CustomException(ErrorCode.RECOMMENDATION_NO_COURSES_FOUND);
        }

        List<Walk> history = walkRepository.findTopByUserIdOrderByStartTimeDesc(
                userId, PageRequest.of(0, 20));

        List<RankedCourse> ranked = reRanking.rerank(
                nearby, req.getLatitude(), req.getLongitude(), radiusKm, history);

        List<CourseRecommendationDto> courses = toDto(ranked);
        return new RecommendationResponse(courses, courses.size());
    }

    private List<CourseRecommendationDto> toDto(List<RankedCourse> ranked) {
        List<CourseRecommendationDto> result = new ArrayList<>();
        for (int i = 0; i < ranked.size(); i++) {
            RankedCourse r = ranked.get(i);
            CourseSearchResult c = r.course();
            result.add(new CourseRecommendationDto(
                    c.id(),
                    i + 1,
                    c.courseName(),
                    c.source(),
                    c.distanceM(),
                    c.estimatedMinutes(),
                    new CourseRecommendationDto.GeoPoint(c.startLat(), c.startLng()),
                    c.endLat() != null
                            ? new CourseRecommendationDto.GeoPoint(c.endLat(), c.endLng())
                            : null,
                    c.tags(),
                    c.difficulty(),
                    Math.round(r.distanceKm() * 10.0) / 10.0,
                    Math.round(r.finalScore() * 100.0) / 100.0
            ));
        }
        return result;
    }
}
