package com.walkit.walkit.domain.recommendation.controller;

import com.walkit.walkit.domain.recommendation.dto.RecommendationRequest;
import com.walkit.walkit.domain.recommendation.dto.RecommendationResponse;
import com.walkit.walkit.domain.recommendation.service.RecommendationService;
import com.walkit.walkit.domain.recommendation.service.RecommendationStreamService;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Tag(name = "추천", description = "감정 기반 산책 코스 추천 API")
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final RecommendationStreamService recommendationStreamService;

    @Operation(
            summary = "감정 기반 코스 추천 (JSON)",
            description = "사용자 감정·위치·원하는 소요시간을 기반으로 산책 코스 최대 3개를 JSON으로 반환."
    )
    @PostMapping("/courses")
    public ResponseEntity<RecommendationResponse> recommend(
            @Valid @RequestBody RecommendationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        RecommendationResponse response = recommendationService.recommend(request, principal.getUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "감정 기반 코스 추천 (SSE 스트리밍)",
            description = """
                    SSE 스트림으로 추천 결과를 순차 전송합니다.
                    이벤트 순서: metadata → course(×N) → reason(스트리밍 토큰) → done
                    - metadata: 요청 감정·날씨 컨텍스트
                    - course: 추천 코스 1건씩 (rank 1부터)
                    - reason: Groq llama가 생성하는 추천 이유 (토큰 단위 스트리밍)
                    - done: 완료 신호 (totalCourses 포함)
                    - error: 오류 발생 시
                    """
    )
    @PostMapping(value = "/courses/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> recommendStream(
            @Valid @RequestBody RecommendationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return recommendationStreamService.stream(request, principal.getUserId());
    }

    @Operation(summary = "코스 상세 조회", description = "특정 코스 상세 정보 조회.")
    @GetMapping("/courses/{id}")
    public ResponseEntity<Object> getCourseDetail(@PathVariable Long id) {
        return ResponseEntity.ok(java.util.Map.of("courseId", id, "message", "상세 조회는 course 이벤트 데이터를 활용하세요"));
    }
}
