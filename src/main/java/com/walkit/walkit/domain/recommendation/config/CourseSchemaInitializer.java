package com.walkit.walkit.domain.recommendation.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CourseSchemaInitializer {

    @Qualifier("knowledgeJdbcTemplate")
    private final JdbcTemplate knowledgeJdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initSchema() {
        log.info("Initializing knowledge base schema...");
        enableVectorExtension();
        createRawCourseDataTable();
        createCourseEmbeddingsTable();
        createIndexes();
        log.info("Knowledge base schema ready.");
    }

    private void enableVectorExtension() {
        knowledgeJdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
    }

    private void createRawCourseDataTable() {
        knowledgeJdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS raw_course_data (
                    id BIGSERIAL PRIMARY KEY,
                    external_id VARCHAR(255) NOT NULL,
                    source VARCHAR(50) NOT NULL,
                    raw_json JSONB NOT NULL,
                    imported_at TIMESTAMP DEFAULT NOW(),
                    processed BOOLEAN DEFAULT FALSE,
                    UNIQUE(source, external_id)
                )
                """);
    }

    private void createCourseEmbeddingsTable() {
        // 기존 Spring AI 스키마(content 컬럼)가 남아있으면 삭제 후 재생성
        Boolean hasOldSchema = knowledgeJdbcTemplate.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1 FROM information_schema.columns
                    WHERE table_name = 'course_embeddings' AND column_name = 'content'
                )
                """, Boolean.class);
        if (Boolean.TRUE.equals(hasOldSchema)) {
            log.warn("course_embeddings가 Spring AI 기본 스키마입니다. 커스텀 스키마로 재생성합니다.");
            knowledgeJdbcTemplate.execute("DROP TABLE IF EXISTS course_embeddings CASCADE");
        }

        knowledgeJdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS course_embeddings (
                    id BIGSERIAL PRIMARY KEY,
                    external_id VARCHAR(255) NOT NULL,
                    source VARCHAR(50) NOT NULL DEFAULT 'SEOUL_OPEN',
                    source_license VARCHAR(50) NOT NULL DEFAULT '공공누리1유형',
                    course_name VARCHAR(255) NOT NULL,
                    description TEXT,
                    start_lat DOUBLE PRECISION NOT NULL,
                    start_lng DOUBLE PRECISION NOT NULL,
                    end_lat DOUBLE PRECISION,
                    end_lng DOUBLE PRECISION,
                    path_geojson JSONB NOT NULL,
                    total_distance INT NOT NULL,
                    estimated_minutes INT NOT NULL,
                    difficulty VARCHAR(20),
                    tags TEXT[],
                    recommended_emotions TEXT[],
                    embedding vector(1536) NOT NULL,
                    embedding_model VARCHAR(50) NOT NULL,
                    tagged_by VARCHAR(50),
                    tagged_at TIMESTAMP,
                    created_at TIMESTAMP DEFAULT NOW(),
                    updated_at TIMESTAMP DEFAULT NOW(),
                    UNIQUE(source, external_id)
                )
                """);
    }

    private void createIndexes() {
        knowledgeJdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_course_embedding_hnsw
                ON course_embeddings USING hnsw (embedding vector_cosine_ops)
                WITH (m = 16, ef_construction = 64)
                """);
        knowledgeJdbcTemplate.execute(
                "CREATE INDEX IF NOT EXISTS idx_course_time_range ON course_embeddings (estimated_minutes)");
        knowledgeJdbcTemplate.execute(
                "CREATE INDEX IF NOT EXISTS idx_course_emotions ON course_embeddings USING GIN (recommended_emotions)");
    }
}
