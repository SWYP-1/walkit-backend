package com.walkit.walkit.domain.recommendation.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walkit.walkit.domain.recommendation.model.Course;
import com.walkit.walkit.domain.recommendation.model.TaggedCourse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Profile("data-load")
@RequiredArgsConstructor
public class LlmTagger {

    private final OpenAiChatModel groqChatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TaggedCourse tag(Course course) {
        String prompt = buildPrompt(course);
        int retries = 0;
        while (retries < 3) {
            try {
                String response = groqChatModel.call(prompt);
                return parseResponse(course, response);
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "";
                if (msg.contains("429") || msg.contains("rate")) {
                    retries++;
                    log.warn("LLM rate limit 도달. 5초 대기 후 재시도 ({}/3)...", retries);
                    sleep(5_000);
                } else {
                    log.warn("LLM 태깅 실패 ({}): {}. 빈 태그로 계속.", course.getCourseName(), msg);
                    break;
                }
            }
        }
        return emptyTagged(course);
    }

    private String buildPrompt(Course course) {
        return """
                다음 산책 코스 정보를 분석하고 아래 카테고리 중 해당 태그를 선택해 JSON으로만 답하세요. JSON 외 다른 텍스트는 출력하지 마세요.

                코스명: %s
                설명: %s
                거리: %dm
                난이도: %s

                태그 후보:
                [환경] 조용함, 활기참, 자연, 도심, 물길, 숲, 공원, 야경
                [인파] 인적드묾, 보통, 붐빔
                [시설] 벤치많음, 화장실, 카페근처

                감정 매핑 대상: JOYFUL, DELIGHTED, HAPPY, DEPRESSED, TIRED, IRRITATED

                응답 형식 (JSON only):
                {
                  "environment": ["..."],
                  "crowd": "...",
                  "facilities": ["..."],
                  "recommended_emotions": ["..."],
                  "reason": "왜 이 감정에 좋은지 한 문장"
                }
                """.formatted(
                course.getCourseName(),
                Optional.ofNullable(course.getDescription()).orElse("(설명 없음)"),
                course.getDistanceM(),
                course.getDifficulty()
        );
    }

    private TaggedCourse parseResponse(Course course, String response) {
        try {
            String json = extractJson(response);
            JsonNode node = objectMapper.readTree(json);

            List<String> environment = parseStringList(node.get("environment"));
            List<String> facilities = parseStringList(node.get("facilities"));
            List<String> emotions = parseStringList(node.get("recommended_emotions"));
            String crowd = node.path("crowd").asText(null);
            String reason = node.path("reason").asText(null);

            return TaggedCourse.builder()
                    .course(course)
                    .environmentTags(environment)
                    .facilityTags(facilities)
                    .crowdTag(crowd)
                    .recommendedEmotions(emotions)
                    .tagReason(reason)
                    .build();
        } catch (Exception e) {
            log.warn("LLM 응답 파싱 실패 ({}): {}", course.getCourseName(), e.getMessage());
            return emptyTagged(course);
        }
    }

    private String extractJson(String response) {
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }

    private List<String> parseStringList(JsonNode node) {
        List<String> result = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                String val = item.asText("").trim();
                if (!val.isBlank()) result.add(val);
            }
        }
        return result;
    }

    private TaggedCourse emptyTagged(Course course) {
        return TaggedCourse.builder().course(course).build();
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
