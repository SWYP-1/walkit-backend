package com.walkit.walkit.domain.recommendation.loader;

import com.walkit.walkit.domain.recommendation.model.TaggedCourse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("data-load")
@RequiredArgsConstructor
public class EmbeddingGenerator {

    private final OpenAiEmbeddingModel openAiEmbeddingModel;

    public float[] embed(TaggedCourse tagged) {
        String text = buildEmbeddingText(tagged);
        log.debug("임베딩 생성: {}", tagged.getCourse().getCourseName());
        return openAiEmbeddingModel.embed(text);
    }

    private String buildEmbeddingText(TaggedCourse tagged) {
        var course = tagged.getCourse();
        String envTags = tagged.getEnvironmentTags().isEmpty() ? ""
                : "환경: " + String.join(", ", tagged.getEnvironmentTags()) + ". ";
        String emotions = tagged.getRecommendedEmotions().isEmpty() ? ""
                : "감정: " + String.join(", ", tagged.getRecommendedEmotions()) + ". ";
        String desc = Optional.ofNullable(course.getDescription()).orElse("");

        return "%s. %s%s%s".formatted(course.getCourseName(), envTags, emotions, desc).trim();
    }
}
