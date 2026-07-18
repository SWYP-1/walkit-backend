package com.walkit.walkit.domain.recommendation.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walkit.walkit.domain.recommendation.model.RawCourseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 한국관광공사 TourAPI v2 클라이언트.
 * contentTypeId=25(여행코스)로 전국 걷기여행길 수집.
 * data.go.kr에서 발급한 일반 인증키(Encoding) 사용.
 */
@Slf4j
@Component
@Profile("data-load")
public class TourApiClient {

    private static final String BASE_URL = "https://apis.data.go.kr/B551011/KorService2";
    private static final int CONTENT_TYPE_WALKING = 25;
    private static final int PAGE_SIZE = 100;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String serviceKey;

    public TourApiClient(@Value("${tour.api.service-key}") String serviceKey) {
        this.serviceKey = serviceKey;
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 전국 걷기여행길 목록 수집 (서울 포함 전국).
     * areaCode 없이 전체 조회 → 900건 이상.
     */
    public List<RawCourseData> fetchAllWalkingCourses() {
        List<RawCourseData> all = new ArrayList<>();
        int pageNo = 1;

        while (true) {
            log.info("TourAPI 수집 중: page {}", pageNo);
            List<RawCourseData> page = fetchPage(pageNo);
            if (page.isEmpty()) break;
            all.addAll(page);
            if (page.size() < PAGE_SIZE) break;
            pageNo++;
        }

        log.info("TourAPI: 총 {}건 수집", all.size());
        return all;
    }

    private List<RawCourseData> fetchPage(int pageNo) {
        try {
            String url = UriComponentsBuilder.fromPath("/areaBasedList2")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("numOfRows", PAGE_SIZE)
                    .queryParam("pageNo", pageNo)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "walkit")
                    .queryParam("contentTypeId", CONTENT_TYPE_WALKING)
                    .queryParam("_type", "json")
                    .build(false)
                    .toUriString();

            String body = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (body == null) return List.of();
            return parseListResponse(body);

        } catch (Exception e) {
            log.error("TourAPI 페이지 {} 조회 실패: {}", pageNo, e.getMessage());
            return List.of();
        }
    }

    private List<RawCourseData> parseListResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode response = root.path("response");
        String resultCode = response.path("header").path("resultCode").asText();

        if (!"0000".equals(resultCode)) {
            log.warn("TourAPI 응답 코드: {} / {}",
                    resultCode, response.path("header").path("resultMsg").asText());
            return List.of();
        }

        JsonNode items = response.path("body").path("items").path("item");
        List<RawCourseData> result = new ArrayList<>();

        if (items.isArray()) {
            for (JsonNode item : items) {
                RawCourseData raw = parseItem(item);
                if (raw != null) result.add(raw);
            }
        } else if (items.isObject()) {
            // 단일 건인 경우 배열이 아닌 객체로 오는 경우 처리
            RawCourseData raw = parseItem(items);
            if (raw != null) result.add(raw);
        }

        return result;
    }

    private RawCourseData parseItem(JsonNode item) {
        try {
            String contentId = item.path("contentid").asText(null);
            String title = item.path("title").asText(null);
            if (contentId == null || title == null || title.isBlank()) return null;

            double mapX = item.path("mapx").asDouble(0);  // 경도
            double mapY = item.path("mapy").asDouble(0);  // 위도
            String addr = item.path("addr1").asText("").trim();

            // TourAPI 목록 API는 설명 미제공 — 주소가 있으면 설명으로 사용, 없으면 null
            String description = addr.isBlank() ? null : addr;

            String rawJson = objectMapper.writeValueAsString(item);

            return RawCourseData.builder()
                    .externalId("TOUR_" + contentId)
                    .source("TOUR_API")
                    .courseName(title)
                    .description(description)
                    .distanceM(null)        // 목록 API에서는 거리 미제공 → DurationEstimator fallback
                    .startLat(mapY != 0 ? mapY : null)
                    .startLng(mapX != 0 ? mapX : null)
                    .path(List.of())
                    .rawJson(rawJson)
                    .build();

        } catch (Exception e) {
            log.debug("TourAPI 항목 파싱 실패: {}", e.getMessage());
            return null;
        }
    }
}
