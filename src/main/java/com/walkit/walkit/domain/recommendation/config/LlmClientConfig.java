package com.walkit.walkit.domain.recommendation.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmClientConfig {

    // LLM 태깅용 — OpenAI gpt-4o-mini (고속, 200K TPM)
    @Bean
    public OpenAiChatModel groqChatModel(@Value("${openai.api-key}") String apiKey) {
        return OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .apiKey(apiKey)
                        .model("gpt-4o-mini")
                        .temperature(0.7)
                        .maxTokens(500)
                        .build())
                .build();
    }

    // Phase 4: Groq llama-3.3-70b-versatile — 추천 이유 스트리밍 생성 전용 (낮은 temperature)
    @Bean("groqResponseChatModel")
    public OpenAiChatModel groqResponseChatModel(
            @Value("${groq.api-key}") String apiKey,
            @Value("${groq.chat.model}") String model) {
        return OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .apiKey(apiKey)
                        .baseUrl("https://api.groq.com/openai/v1")
                        .model(model)
                        .temperature(0.5)
                        .maxTokens(1024)
                        .build())
                .build();
    }

    // OpenAI — Embedding 전용 (Groq은 embedding 미지원)
    @Bean
    public OpenAiEmbeddingModel openAiEmbeddingModel(@Value("${openai.api-key}") String apiKey) {
        return OpenAiEmbeddingModel.builder()
                .options(OpenAiEmbeddingOptions.builder()
                        .apiKey(apiKey)
                        .model("text-embedding-3-small")
                        .build())
                .metadataMode(MetadataMode.EMBED)
                .build();
    }
}
