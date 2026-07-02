package com.walkit.walkit.domain.recommendation.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walkit.walkit.domain.recommendation.model.RawCourseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenStreetMap Overpass API 클라이언트.
 * 서울 bounding box 내 named hiking/walking route relation을 수집.
 * 인증 불필요, ODbL 라이선스 (출처 표시 필요).
 */
@Slf4j
@Component
@Profile("data-load")
public class OsmOverpassClient {

    // 서울 bounding box (south, west, north, east)
    private static final String SEOUL_BBOX = "37.42,126.77,37.70,127.18";
    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OsmOverpassClient() {
        this.webClient = WebClient.builder()
                .baseUrl(OVERPASS_URL)
                .codecs(c -> c.defaultCodecs().maxInMemorySize(20 * 1024 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<RawCourseData> fetchSeoulWalkingRoutes() {
        log.info("OSM Overpass API에서 서울 walking routes 수집 중...");
        String query = buildQuery();

        try {
            String body = webClient.post()
                    .uri("")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue("data=" + URLEncoder.encode(query, StandardCharsets.UTF_8))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (body == null) return List.of();

            return parseResponse(body);
        } catch (Exception e) {
            log.error("OSM API 호출 실패: {}", e.getMessage());
            return List.of();
        }
    }

    private String buildQuery() {
        return """
                [out:json][timeout:120];
                (
                  relation["route"~"hiking|foot"]["name"](%s);
                  relation["route"="walking"]["name"](%s);
                );
                out body;
                """.formatted(SEOUL_BBOX, SEOUL_BBOX);
    }

    private List<RawCourseData> parseResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode elements = root.get("elements");
        if (elements == null || !elements.isArray()) return List.of();

        List<RawCourseData> result = new ArrayList<>();
        for (JsonNode element : elements) {
            RawCourseData raw = parseElement(element);
            if (raw != null) result.add(raw);
        }

        log.info("OSM: {}건 파싱 완료", result.size());
        return result;
    }

    private RawCourseData parseElement(JsonNode element) {
        try {
            JsonNode tags = element.get("tags");
            if (tags == null) return null;

            String name = tags.path("name").asText(null);
            if (name == null || name.isBlank()) return null;

            String osmId = element.path("type").asText("relation")
                    + "/" + element.path("id").asText();

            String description = tags.path("description").asText(null);
            if (description == null) description = tags.path("note").asText(null);

            // 거리: distance 태그 (km 단위인 경우 m로 변환)
            Integer distanceM = parseDistance(tags.path("distance").asText(null));

            // 난이도
            String difficulty = tags.path("sac_scale").asText(null);
            if (difficulty == null) difficulty = tags.path("difficulty").asText(null);

            String rawJson = objectMapper.writeValueAsString(element);

            return RawCourseData.builder()
                    .externalId(osmId)
                    .source("OSM")
                    .courseName(name)
                    .description(description)
                    .distanceM(distanceM)
                    .level(difficulty)
                    .path(List.of())
                    .rawJson(rawJson)
                    .build();

        } catch (Exception e) {
            log.debug("OSM 요소 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    private Integer parseDistance(String distanceStr) {
        if (distanceStr == null || distanceStr.isBlank()) return null;
        try {
            // "5.2 km", "5200 m", "5.2" 등 다양한 형식 처리
            String cleaned = distanceStr.replaceAll("[^0-9.]", "").trim();
            if (cleaned.isEmpty()) return null;
            double val = Double.parseDouble(cleaned);
            // km인지 m인지 판단: 100 이하면 km로 간주
            if (val < 100) return (int) (val * 1000);
            return (int) val;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
