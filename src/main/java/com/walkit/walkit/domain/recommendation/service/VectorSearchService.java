package com.walkit.walkit.domain.recommendation.service;

import com.walkit.walkit.domain.recommendation.model.CourseSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchService {

    @Qualifier("knowledgeJdbcTemplate")
    private final JdbcTemplate jdbc;

    private static final int TOP_CANDIDATES = 20;

    public List<CourseSearchResult> search(float[] queryEmbedding, int desiredMinutes) {
        int minMinutes = Math.max(10, (int) (desiredMinutes * 0.5));
        int maxMinutes = Math.min(180, (int) (desiredMinutes * 1.5));
        String vectorLiteral = toVectorLiteral(queryEmbedding);

        log.debug("벡터 검색: desiredMinutes={}, range=[{}, {}]", desiredMinutes, minMinutes, maxMinutes);

        // <=> 는 pgvector 코사인 거리. 1 - 거리 = 유사도.
        String sql = """
                SELECT id, external_id, source, course_name, description,
                       start_lat, start_lng, end_lat, end_lng,
                       total_distance, estimated_minutes, difficulty,
                       tags, recommended_emotions,
                       1 - (embedding <=> ?::vector) AS similarity_score
                FROM course_embeddings
                WHERE estimated_minutes BETWEEN ? AND ?
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """;

        List<CourseSearchResult> results = jdbc.query(sql,
                ps -> {
                    ps.setString(1, vectorLiteral);
                    ps.setInt(2, minMinutes);
                    ps.setInt(3, maxMinutes);
                    ps.setString(4, vectorLiteral);
                    ps.setInt(5, TOP_CANDIDATES);
                },
                (rs, rowNum) -> new CourseSearchResult(
                        rs.getLong("id"),
                        rs.getString("external_id"),
                        rs.getString("source"),
                        rs.getString("course_name"),
                        rs.getString("description"),
                        rs.getDouble("start_lat"),
                        rs.getDouble("start_lng"),
                        (Double) rs.getObject("end_lat"),
                        (Double) rs.getObject("end_lng"),
                        rs.getInt("total_distance"),
                        rs.getInt("estimated_minutes"),
                        rs.getString("difficulty"),
                        toStringList(rs.getArray("tags")),
                        toStringList(rs.getArray("recommended_emotions")),
                        rs.getDouble("similarity_score")
                ));

        log.debug("벡터 검색 결과: {}개", results.size());
        return results;
    }

    private List<String> toStringList(Array arr) {
        if (arr == null) return List.of();
        try {
            return Arrays.asList((String[]) arr.getArray());
        } catch (Exception e) {
            return List.of();
        }
    }

    private String toVectorLiteral(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) sb.append(',');
        }
        sb.append("]");
        return sb.toString();
    }
}
