package com.walkit.walkit.domain.recommendation.service;

import com.walkit.walkit.domain.recommendation.dto.CourseRecommendationDto;
import com.walkit.walkit.domain.recommendation.model.Emotion;
import com.walkit.walkit.domain.weather.dto.CurrentWeatherResponseDto;
import com.walkit.walkit.domain.weather.entity.PrecipType;
import com.walkit.walkit.domain.weather.entity.SkyStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
public class LlmResponseService {

    private final OpenAiChatModel groqResponseChatModel;

    @Value("${recommendation.prompt.system}")
    private String systemPromptTemplate;

    @Value("${recommendation.prompt.user}")
    private String userPromptTemplate;

    public LlmResponseService(@Qualifier("groqResponseChatModel") OpenAiChatModel groqResponseChatModel) {
        this.groqResponseChatModel = groqResponseChatModel;
    }

    public Flux<ServerSentEvent<String>> streamReason(
            List<CourseRecommendationDto> courses,
            Emotion emotion,
            int desiredMinutes,
            CurrentWeatherResponseDto weather) {

        String userPrompt = buildUserPrompt(courses, emotion, desiredMinutes, weather);
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemPromptTemplate),
                new UserMessage(userPrompt)
        ));

        log.debug("Groq 스트리밍 시작 — 감정={}, 코스 수={}", emotion, courses.size());

        return groqResponseChatModel.stream(prompt)
                .map(response -> {
                    if (response == null || response.getResult() == null) return "";
                    var output = response.getResult().getOutput();
                    if (output == null) return "";
                    String text = output.getText();
                    return text != null ? text : "";
                })
                .filter(text -> !text.isEmpty())
                .doOnNext(token -> log.trace("reason 토큰: {}", token))
                .map(token -> ServerSentEvent.<String>builder()
                        .event("reason")
                        .data(token)
                        .build())
                .onErrorResume(e -> {
                    log.error("Groq 스트리밍 오류", e);
                    return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data("{\"message\":\"추천 이유를 생성하는 중 오류가 발생했습니다\"}")
                            .build());
                });
    }

    private String buildUserPrompt(
            List<CourseRecommendationDto> courses,
            Emotion emotion,
            int desiredMinutes,
            CurrentWeatherResponseDto weather) {

        return userPromptTemplate
                .replace("{emotion}", emotionToKorean(emotion))
                .replace("{desiredMinutes}", String.valueOf(desiredMinutes))
                .replace("{weatherSummary}", buildWeatherSummary(weather))
                .replace("{courseCount}", String.valueOf(courses.size()))
                .replace("{courseSummary}", buildCourseSummary(courses));
    }

    private String buildCourseSummary(List<CourseRecommendationDto> courses) {
        StringBuilder sb = new StringBuilder();
        for (CourseRecommendationDto c : courses) {
            sb.append(String.format("[%d위] %s%n", c.rank(), c.name()));
            sb.append(String.format("- 거리: %.1fkm / 소요시간: %d분 / 난이도: %s%n",
                    c.distance() / 1000.0, c.estimatedMinutes(), c.difficulty()));
            if (!c.tags().isEmpty()) {
                sb.append(String.format("- 태그: %s%n", String.join(", ", c.tags())));
            }
            sb.append(String.format("- 사용자로부터 %.1fkm 거리%n%n", c.distanceKm()));
        }
        return sb.toString().trim();
    }

    private String buildWeatherSummary(CurrentWeatherResponseDto weather) {
        if (weather == null) return "정보 없음";

        String sky = switch (weather.getSky()) {
            case SUNNY -> "맑음";
            case CLOUDY_MANY -> "구름 많음";
            case OVERCAST -> "흐림";
            default -> "알 수 없음";
        };

        String precip = weather.getPrecipType() != PrecipType.NONE && weather.getPrecipType() != PrecipType.UNKNOWN
                ? " (" + precipToKorean(weather.getPrecipType()) + ")"
                : "";

        return String.format("기온 %.1f°C, %s%s", weather.getTempC(), sky, precip);
    }

    private String precipToKorean(PrecipType type) {
        return switch (type) {
            case RAIN -> "비";
            case RAIN_SNOW -> "비/눈";
            case SNOW -> "눈";
            case SHOWER -> "소나기";
            default -> "";
        };
    }

    private String emotionToKorean(Emotion emotion) {
        return switch (emotion) {
            case JOYFUL -> "즐거움";
            case DELIGHTED -> "신남";
            case HAPPY -> "행복함";
            case DEPRESSED -> "우울함";
            case TIRED -> "피로함";
            case IRRITATED -> "짜증남";
        };
    }
}
