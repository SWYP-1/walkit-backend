package com.walkit.walkit.domain.recommendation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Profile("!prod")
@RequiredArgsConstructor
public class HelloTestController {

    private final PgVectorStore pgVectorStore;
    private final OpenAiChatModel groqChatModel;

    // Hello Test 1: OpenAI 임베딩 → pgvector 저장 → 코사인 유사도 검색
    @PostMapping("/hello1")
    public Map<String, Object> testEmbedding(@RequestBody Map<String, String> req) {
        String text = req.get("text");
        pgVectorStore.add(List.of(new Document(text)));

        List<Document> results = pgVectorStore.similaritySearch(
                SearchRequest.builder().query(text).topK(3).build());

        return Map.of(
                "stored", text,
                "nearest", results.stream().map(Document::getText).toList()
        );
    }

    // Hello Test 2: Groq Chat 응답 확인
    @PostMapping("/hello2")
    public Map<String, String> testGroqChat(@RequestBody Map<String, String> req) {
        String response = groqChatModel.call(req.get("prompt"));
        return Map.of("response", response);
    }

    // Hello Test 3: Groq 스트리밍 SSE
    @GetMapping(value = "/hello3", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> testGroqStream(@RequestParam String prompt) {
        return groqChatModel.stream(new Prompt(prompt))
                .map(chatResponse -> chatResponse.getResult().getOutput().getText());
    }
}
