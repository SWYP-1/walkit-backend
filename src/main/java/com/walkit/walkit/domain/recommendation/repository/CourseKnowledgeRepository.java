package com.walkit.walkit.domain.recommendation.repository;

import com.walkit.walkit.domain.recommendation.model.RawCourseData;
import com.walkit.walkit.domain.recommendation.model.TaggedCourse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CourseKnowledgeRepository {

    @Qualifier("knowledgeJdbcTemplate")
    private final JdbcTemplate jdbc;

    public void upsertRaw(RawCourseData raw) {
        jdbc.update("""
                INSERT INTO raw_course_data (external_id, source, raw_json)
                VALUES (?, ?, ?::jsonb)
                ON CONFLICT (source, external_id) DO NOTHING
                """,
                raw.getExternalId(), raw.getSource(), raw.getRawJson());
    }

    public void upsertCourseEmbedding(TaggedCourse tagged, float[] embedding) {
        var course = tagged.getCourse();
        String embeddingLiteral = toVectorLiteral(embedding);

        jdbc.update("""
                INSERT INTO course_embeddings (
                    external_id, source, source_license,
                    course_name, description,
                    start_lat, start_lng, end_lat, end_lng,
                    path_geojson, total_distance, estimated_minutes, difficulty,
                    tags, recommended_emotions,
                    embedding, embedding_model,
                    tagged_by, tagged_at, updated_at
                ) VALUES (
                    ?, ?, '공공누리1유형',
                    ?, ?,
                    ?, ?, ?, ?,
                    ?::jsonb, ?, ?, ?,
                    ?, ?,
                    ?::vector, 'text-embedding-3-small',
                    'GPT_4O_MINI', ?, NOW()
                )
                ON CONFLICT (source, external_id) DO UPDATE SET
                    course_name = EXCLUDED.course_name,
                    description = EXCLUDED.description,
                    total_distance = EXCLUDED.total_distance,
                    estimated_minutes = EXCLUDED.estimated_minutes,
                    difficulty = EXCLUDED.difficulty,
                    tags = EXCLUDED.tags,
                    recommended_emotions = EXCLUDED.recommended_emotions,
                    embedding = EXCLUDED.embedding,
                    embedding_model = EXCLUDED.embedding_model,
                    tagged_by = EXCLUDED.tagged_by,
                    tagged_at = EXCLUDED.tagged_at,
                    updated_at = NOW()
                """,
                ps -> {
                    ps.setString(1, course.getExternalId());
                    ps.setString(2, course.getSource());
                    ps.setString(3, course.getCourseName());
                    ps.setString(4, course.getDescription());
                    ps.setDouble(5, course.getStartLat());
                    ps.setDouble(6, course.getStartLng());
                    if (course.getEndLat() != null) ps.setDouble(7, course.getEndLat());
                    else ps.setNull(7, java.sql.Types.DOUBLE);
                    if (course.getEndLng() != null) ps.setDouble(8, course.getEndLng());
                    else ps.setNull(8, java.sql.Types.DOUBLE);
                    ps.setString(9, toPathGeoJson(course.getPath()));
                    ps.setInt(10, course.getDistanceM());
                    ps.setInt(11, course.getEstimatedMinutes());
                    ps.setString(12, course.getDifficulty() != null ? course.getDifficulty().name() : null);
                    Array tagsArr = ps.getConnection().createArrayOf("text",
                            allTags(tagged).toArray(new String[0]));
                    ps.setArray(13, tagsArr);
                    Array emotionsArr = ps.getConnection().createArrayOf("text",
                            tagged.getRecommendedEmotions().toArray(new String[0]));
                    ps.setArray(14, emotionsArr);
                    ps.setString(15, embeddingLiteral);
                    ps.setTimestamp(16, Timestamp.valueOf(LocalDateTime.now()));
                });
    }

    public boolean isProperlyTagged(String externalId, String source) {
        // recommended_emotions = {HAPPY} 단독인 경우 → LLM 태깅 실패 fallback → 재처리 대상
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM course_embeddings WHERE source = ? AND external_id = ? " +
                "AND NOT (recommended_emotions = ARRAY['HAPPY']::text[])",
                Integer.class, source, externalId);
        return count != null && count > 0;
    }

    public int countCourseEmbeddings() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM course_embeddings", Integer.class);
        return count != null ? count : 0;
    }

    public int countRawCourseData() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM raw_course_data", Integer.class);
        return count != null ? count : 0;
    }

    private String toVectorLiteral(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private List<String> allTags(TaggedCourse tagged) {
        var all = new java.util.ArrayList<String>();
        all.addAll(tagged.getEnvironmentTags());
        all.addAll(tagged.getFacilityTags());
        if (tagged.getCrowdTag() != null) all.add(tagged.getCrowdTag());
        return all;
    }

    private String toPathGeoJson(List<double[]> path) {
        if (path == null || path.isEmpty()) return "{\"type\":\"LineString\",\"coordinates\":[]}";
        StringBuilder sb = new StringBuilder("{\"type\":\"LineString\",\"coordinates\":[");
        for (int i = 0; i < path.size(); i++) {
            double[] coord = path.get(i);
            sb.append("[").append(coord[1]).append(",").append(coord[0]).append("]");
            if (i < path.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }
}
