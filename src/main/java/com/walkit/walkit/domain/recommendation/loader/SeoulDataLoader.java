package com.walkit.walkit.domain.recommendation.loader;

import com.walkit.walkit.domain.recommendation.model.Course;
import com.walkit.walkit.domain.recommendation.model.RawCourseData;
import com.walkit.walkit.domain.recommendation.model.TaggedCourse;
import com.walkit.walkit.domain.recommendation.repository.CourseKnowledgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Profile("data-load")
@RequiredArgsConstructor
public class SeoulDataLoader implements CommandLineRunner {

    private final SeoulOpenDataClient seoulOpenDataClient;
    private final TourApiClient tourApiClient;
    private final OsmOverpassClient osmClient;
    private final CourseNormalizer normalizer;
    private final LlmTagger tagger;
    private final TagValidator validator;
    private final EmbeddingGenerator embedder;
    private final CourseKnowledgeRepository repo;

    @Override
    public void run(String... args) {
        log.info("=== 데이터 로드 파이프라인 시작 ===");

        // Step 1: 다중 소스 수집
        List<RawCourseData> rawList = collectFromAllSources();
        rawList.forEach(repo::upsertRaw);
        log.info("[Step 1] 완료: 총 {}건 수집 및 staging 저장", rawList.size());

        // Step 2: 정규화
        log.info("[Step 2] 정규화 중...");
        List<Course> normalized = rawList.stream()
                .map(normalizer::normalize)
                .toList();
        log.info("[Step 2] 완료: {}건 정규화", normalized.size());

        // Step 3-5: 코스별 순차 처리 (LLM 태깅 → 임베딩 → 저장)
        // 중단 후 재실행 시 이미 태깅된 코스는 스킵
        log.info("[Step 3] LLM 태깅 + 임베딩 + 저장 중 (코스별 순차 처리, 재시작 가능)...");
        int saved = 0, skipped = 0;
        for (Course course : normalized) {
            if (repo.isProperlyTagged(course.getExternalId(), course.getSource())) {
                skipped++;
                continue;
            }
            try {
                TaggedCourse tc = validator.validateAndFix(tagger.tag(course));
                float[] embedding = embedder.embed(tc);
                repo.upsertCourseEmbedding(tc, embedding);
                saved++;
                if (saved % 20 == 0) {
                    log.info("  진행: {}/{} (저장: {}, 스킵: {})", saved + skipped, normalized.size(), saved, skipped);
                }
            } catch (Exception e) {
                log.warn("  처리 실패 ({}): {}", course.getCourseName(), e.getMessage());
            }
        }

        log.info("[Step 3] 완료: {}건 저장, {}건 스킵", saved, skipped);
        log.info("=== 파이프라인 완료 ===");
        log.info("raw_course_data: {}건", repo.countRawCourseData());
        log.info("course_embeddings: {}건", repo.countCourseEmbeddings());
    }

    private List<RawCourseData> collectFromAllSources() {
        List<RawCourseData> all = new ArrayList<>();

        // 서울열린데이터광장: 서울둘레길
        log.info("[Step 1-A] 서울열린데이터 수집 중...");
        List<RawCourseData> seoulData = seoulOpenDataClient.fetchAllWalkingCourses();
        all.addAll(seoulData);
        log.info("[Step 1-A] 서울열린데이터: {}건", seoulData.size());

        // TourAPI: 전국 걷기여행길
        log.info("[Step 1-B] TourAPI 수집 중...");
        List<RawCourseData> tourData = tourApiClient.fetchAllWalkingCourses();
        all.addAll(tourData);
        log.info("[Step 1-B] TourAPI: {}건", tourData.size());

        // OSM: 서울 hiking/walking routes
        log.info("[Step 1-C] OSM Overpass 수집 중...");
        List<RawCourseData> osmData = osmClient.fetchSeoulWalkingRoutes();
        all.addAll(osmData);
        log.info("[Step 1-C] OSM: {}건", osmData.size());

        return all;
    }
}
