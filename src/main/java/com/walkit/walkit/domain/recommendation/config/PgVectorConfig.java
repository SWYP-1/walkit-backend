package com.walkit.walkit.domain.recommendation.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PgVectorConfig {

    // Spring AI PgVectorStoreAutoConfiguration은 @Primary DataSource(MySQL)를 사용하므로
    // 수동 빈으로 오버라이드해 knowledge datasource(PostgreSQL)를 주입
    @Bean
    public PgVectorStore pgVectorStore(
            @Qualifier("knowledgeJdbcTemplate") JdbcTemplate jdbcTemplate,
            EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName("spring_ai_hello_store")
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .dimensions(1536)
                .initializeSchema(true)
                .build();
    }
}
