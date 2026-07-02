package com.walkit.walkit.domain.recommendation.service;

import com.walkit.walkit.domain.recommendation.model.Emotion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final OpenAiEmbeddingModel openAiEmbeddingModel;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String CACHE_PREFIX = "embedding:query:v1:";
    private static final Duration CACHE_TTL = Duration.ofHours(6);

    public float[] embedQuery(Emotion emotion, int desiredMinutes) {
        String cacheKey = CACHE_PREFIX + emotion.name() + ":" + minutesBucket(desiredMinutes);
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("임베딩 캐시 히트: {}", cacheKey);
                return deserialize(cached);
            }
        } catch (Exception e) {
            log.warn("Redis 캐시 조회 실패, 임베딩 직접 생성: {}", e.getMessage());
        }

        String queryText = buildQueryText(emotion);
        log.debug("OpenAI 임베딩 생성: emotion={}", emotion);
        float[] embedding = openAiEmbeddingModel.embed(queryText);

        try {
            stringRedisTemplate.opsForValue().set(cacheKey, serialize(embedding), CACHE_TTL);
        } catch (Exception e) {
            log.warn("Redis 캐시 저장 실패: {}", e.getMessage());
        }
        return embedding;
    }

    private String buildQueryText(Emotion emotion) {
        return switch (emotion) {
            case DEPRESSED -> "우울하고 힘들 때 위안이 되는 조용하고 편안한 산책로. 자연 속에서 혼자 걷기 좋음. 감정: 우울함 DEPRESSED";
            case TIRED -> "지치고 피곤할 때 가볍게 걷기 좋은 평탄한 짧은 산책로. 무리하지 않고 느긋하게. 감정: 피로함 TIRED";
            case IRRITATED -> "화나고 답답할 때 기분 전환에 좋은 탁 트인 넓은 산책로. 스트레스 해소. 감정: 짜증남 IRRITATED";
            case JOYFUL -> "기쁘고 즐거울 때 활기차게 걷기 좋은 아름다운 산책로. 기분 좋은 경치. 감정: 즐거움 JOYFUL";
            case DELIGHTED -> "신나고 들뜬 기분에 어울리는 경치 좋고 다채로운 산책로. 활기찬 분위기. 감정: 신남 DELIGHTED";
            case HAPPY -> "행복하고 만족스러운 기분에 어울리는 편안하고 아름다운 산책로. 감정: 행복함 HAPPY";
        };
    }

    // 10분 단위 버킷으로 캐시 키 공유 (20분 ≈ 25분 동일 쿼리 사용)
    private int minutesBucket(int minutes) {
        return (minutes / 10) * 10;
    }

    private String serialize(float[] embedding) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) sb.append(',');
        }
        return sb.toString();
    }

    private float[] deserialize(String s) {
        String[] parts = s.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i]);
        }
        return result;
    }
}
