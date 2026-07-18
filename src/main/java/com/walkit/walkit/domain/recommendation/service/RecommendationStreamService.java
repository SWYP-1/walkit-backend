package com.walkit.walkit.domain.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walkit.walkit.domain.recommendation.dto.CourseRecommendationDto;
import com.walkit.walkit.domain.recommendation.dto.RecommendationRequest;
import com.walkit.walkit.domain.recommendation.dto.RecommendationResponse;
import com.walkit.walkit.domain.weather.dto.CurrentWeatherResponseDto;
import com.walkit.walkit.domain.weather.service.WeatherService;
import com.walkit.walkit.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationStreamService {

    private final RecommendationService recommendationService;
    private final WeatherService weatherService;
    private final LlmResponseService llmResponseService;
    private final ObjectMapper objectMapper;

    public Flux<ServerSentEvent<String>> stream(RecommendationRequest req, Long userId) {
        return Mono.fromCallable(() -> fetchContext(req, userId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(ctx -> buildStream(req, ctx))
                .onErrorResume(CustomException.class, e -> {
                    log.warn("추천 오류: {}", e.getErrorCode().getMessage());
                    return Flux.just(errorEvent(e.getErrorCode().getMessage()));
                })
                .onErrorResume(e -> {
                    log.error("SSE 스트림 예외", e);
                    return Flux.just(errorEvent("추천 처리 중 오류가 발생했습니다"));
                });
    }

    private record StreamContext(RecommendationResponse recommendations, CurrentWeatherResponseDto weather) {}

    private StreamContext fetchContext(RecommendationRequest req, Long userId) {
        RecommendationResponse recommendations = recommendationService.recommend(req, userId);
        CurrentWeatherResponseDto weather = getWeatherSafely(req.getLatitude(), req.getLongitude());
        return new StreamContext(recommendations, weather);
    }

    private CurrentWeatherResponseDto getWeatherSafely(double lat, double lon) {
        try {
            return weatherService.getCurrent(lat, lon);
        } catch (Exception e) {
            log.warn("날씨 조회 실패 (무시하고 계속): {}", e.getMessage());
            return null;
        }
    }

    private Flux<ServerSentEvent<String>> buildStream(RecommendationRequest req, StreamContext ctx) {
        List<CourseRecommendationDto> courses = ctx.recommendations().courses();

        Flux<ServerSentEvent<String>> metadataEvent = Flux.just(buildMetadataEvent(req, ctx.weather()));
        Flux<ServerSentEvent<String>> courseEvents = Flux.fromIterable(courses)
                .map(this::buildCourseEvent);
        Flux<ServerSentEvent<String>> reasonStream = llmResponseService.streamReason(
                courses, req.getEmotion(), req.getDesiredMinutes(), ctx.weather());
        Flux<ServerSentEvent<String>> doneEvent = Flux.just(buildDoneEvent(courses.size()));

        return Flux.concat(metadataEvent, courseEvents, reasonStream, doneEvent);
    }

    private ServerSentEvent<String> buildMetadataEvent(RecommendationRequest req, CurrentWeatherResponseDto weather) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("emotion", req.getEmotion().name());
        payload.put("desiredMinutes", req.getDesiredMinutes());
        payload.put("radiusKm", req.getRadiusKm() != null ? req.getRadiusKm() : 5.0);
        if (weather != null) {
            payload.put("weather", Map.of(
                    "tempC", weather.getTempC(),
                    "sky", weather.getSky().name(),
                    "precipType", weather.getPrecipType().name()
            ));
        }
        return ServerSentEvent.<String>builder()
                .event("metadata")
                .data(toJson(payload))
                .build();
    }

    private ServerSentEvent<String> buildCourseEvent(CourseRecommendationDto course) {
        return ServerSentEvent.<String>builder()
                .event("course")
                .data(toJson(course))
                .build();
    }

    private ServerSentEvent<String> buildDoneEvent(int totalCourses) {
        return ServerSentEvent.<String>builder()
                .event("done")
                .data(toJson(Map.of("totalCourses", totalCourses)))
                .build();
    }

    private ServerSentEvent<String> errorEvent(String message) {
        return ServerSentEvent.<String>builder()
                .event("error")
                .data(toJson(Map.of("message", message)))
                .build();
    }

    private String toJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패", e);
            return "{}";
        }
    }
}
