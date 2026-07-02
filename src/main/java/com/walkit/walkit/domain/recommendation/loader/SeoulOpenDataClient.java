package com.walkit.walkit.domain.recommendation.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walkit.walkit.domain.recommendation.model.RawCourseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

/**
 * 서울열린데이터광장 산책로 API 클라이언트.
 * URL 패턴: http://openapi.seoul.go.kr:8088/{apiKey}/json/{serviceName}/{start}/{end}/
 *
 * 실제 서비스명은 data.seoul.go.kr 에서 확인 후 application.yml에 설정.
 * 현재 기본값: SeoulDulegil (서울둘레길)
 */
@Slf4j
@Component
@Profile("data-load")
public class SeoulOpenDataClient {

    private static final int PAGE_SIZE = 1000;
    private static final String BASE_URL = "http://openapi.seoul.go.kr:8088";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String serviceName;

    public SeoulOpenDataClient(
            @Value("${seoul.open-data.api-key}") String apiKey,
            @Value("${seoul.open-data.service-name:SeoulDulegil}") String serviceName) {
        this.apiKey = apiKey;
        this.serviceName = serviceName;
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<RawCourseData> fetchAllWalkingCourses() {
        List<RawCourseData> all = new ArrayList<>();
        int start = 1;

        while (true) {
            int end = start + PAGE_SIZE - 1;
            String url = "/%s/json/%s/%d/%d/".formatted(apiKey, serviceName, start, end);
            log.info("Fetching Seoul walking courses: {} ~ {}", start, end);

            try {
                String body = webClient.get().uri(url)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                if (body == null) break;

                JsonNode root = objectMapper.readTree(body);
                JsonNode serviceNode = root.get(serviceName);
                if (serviceNode == null || serviceNode.get("RESULT") == null) {
                    log.warn("서비스명 '{}' 응답이 없습니다. data.seoul.go.kr에서 정확한 서비스명을 확인하세요.", serviceName);
                    log.warn("응답 루트 키: {}", root.fieldNames());
                    break;
                }

                JsonNode resultNode = serviceNode.get("RESULT");
                String resultCode = resultNode.path("CODE").asText();
                if (!resultCode.startsWith("INFO-000")) {
                    log.warn("API 결과 코드: {} / {}", resultCode, resultNode.path("MESSAGE").asText());
                    break;
                }

                int totalCount = serviceNode.get("list_total_count").asInt();
                JsonNode rows = serviceNode.get("row");

                if (rows == null || !rows.isArray()) break;

                for (JsonNode row : rows) {
                    RawCourseData raw = parseRow(row);
                    if (raw != null) all.add(raw);
                }

                log.info("수집 완료: {}건 / 전체 {}건", all.size(), totalCount);
                if (end >= totalCount) break;
                start = end + 1;

            } catch (Exception e) {
                log.error("API 호출 실패 (start={}): {}", start, e.getMessage());
                break;
            }
        }

        log.info("총 {}건 수집 완료", all.size());
        return all;
    }

    private RawCourseData parseRow(JsonNode row) {
        try {
            String rawJson = objectMapper.writeValueAsString(row);

            // 서울둘레길 API 필드명 (실제 API 응답에 따라 조정 필요)
            String externalId = firstNonNull(row,
                    "DULEGIL_NO", "COURSE_ID", "COURSE_NO", "WRK_PLAN_NO", "FLD_NUM");
            String courseName = firstNonNull(row,
                    "DULEGIL_NM", "COURSE_NM", "COURSE_NAME", "WRK_PLAN_NM");
            String description = firstNonNull(row,
                    "DULEGIL_INTRO", "COURSE_DC", "DESCRIPTION", "INTRO");
            Integer distanceM = parseIntField(row,
                    "DULEGIL_LEN", "COURSE_LEN", "COURSE_DISTANCE", "LEN");
            Double startLat = parseDoubleField(row,
                    "STPT_LA", "START_LAT", "LAT", "Y_DNTS");
            Double startLng = parseDoubleField(row,
                    "STPT_LO", "START_LNG", "LNG", "X_DNTS");
            String level = firstNonNull(row,
                    "DULEGIL_LEVEL", "COURSE_LEVEL", "DIFFICULTY", "LEVEL_NM");

            if (externalId == null || courseName == null) {
                log.debug("필수 필드 누락 (externalId={}, courseName={}). 행 건너뜀.", externalId, courseName);
                return null;
            }

            return RawCourseData.builder()
                    .externalId(externalId)
                    .source("SEOUL_OPEN")
                    .courseName(courseName)
                    .description(description)
                    .distanceM(distanceM)
                    .startLat(startLat)
                    .startLng(startLng)
                    .level(level)
                    .path(List.of())
                    .rawJson(rawJson)
                    .build();

        } catch (Exception e) {
            log.warn("행 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    private String firstNonNull(JsonNode row, String... fieldNames) {
        for (String field : fieldNames) {
            JsonNode node = row.get(field);
            if (node != null && !node.isNull() && !node.asText().isBlank()) {
                return node.asText().trim();
            }
        }
        return null;
    }

    private Integer parseIntField(JsonNode row, String... fieldNames) {
        String val = firstNonNull(row, fieldNames);
        if (val == null) return null;
        try {
            return (int) Double.parseDouble(val.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDoubleField(JsonNode row, String... fieldNames) {
        String val = firstNonNull(row, fieldNames);
        if (val == null) return null;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
