package com.walkit.walkit.domain.spot.service;

import com.walkit.walkit.domain.spot.client.KakaoLocalClient;
import com.walkit.walkit.domain.spot.client.NaverSearchClient;
import com.walkit.walkit.domain.spot.dto.response.NearbySpotPageResponseDto;
import com.walkit.walkit.domain.spot.dto.response.NearbySpotResponseDto;
import com.walkit.walkit.domain.spot.enums.SpotCategoryImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotService {

    private final KakaoLocalClient kakaoLocalClient;
    private final NaverSearchClient naverSearchClient;

    @Value("https://kr.object.ncloudstorage.com/${ncp.bucket-name}")
    private String ncpBaseUrl;

    // 네이버 API Rate Limit 대응: 최대 5개 장소 동시 처리
    private final Semaphore semaphore = new Semaphore(5);
    // 장소별 enrichPlace 작업용 (outer)
    private final ExecutorService placeExecutor = Executors.newFixedThreadPool(5);
    // 네이버 블로그 호출용 (inner) — placeExecutor와 분리하여 데드락 방지
    private final ExecutorService naverExecutor = Executors.newFixedThreadPool(10);

    @PreDestroy
    public void shutdown() {
        placeExecutor.shutdown();
        naverExecutor.shutdown();
    }

    public NearbySpotPageResponseDto getNearbySpots(
            String query, double x, double y, int radius, int size, String sort, String cursor
    ) {
        int page = parseCursor(cursor);
        List<KakaoLocalClient.KakaoPlace> places;
        boolean hasNext;

        if (query == null || query.isBlank()) {
            int perCategorySize = Math.max(1, (int) Math.ceil((double) size / SpotCategoryImage.values().length));

            List<CompletableFuture<KakaoLocalClient.KakaoLocalResponse>> categoryFutures =
                    Arrays.stream(SpotCategoryImage.values())
                            .map(cat -> CompletableFuture.supplyAsync(
                                    () -> kakaoLocalClient.searchCategory(cat.getKakaoGroupCode(), x, y, radius, perCategorySize, page, sort),
                                    placeExecutor))
                            .toList();

            List<KakaoLocalClient.KakaoLocalResponse> responses = categoryFutures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            places = responses.stream()
                    .flatMap(r -> r.getDocuments().stream())
                    .distinct()
                    .sorted(Comparator.comparingInt(p -> parseDistance(p.getDistance())))
                    .limit(size)
                    .toList();

            hasNext = responses.stream()
                    .anyMatch(r -> r.getMeta() != null && !r.getMeta().isEnd());
        } else {
            KakaoLocalClient.KakaoLocalResponse response =
                    kakaoLocalClient.searchKeyword(query, x, y, radius, size, page, sort);
            places = response.getDocuments();
            hasNext = response.getMeta() != null && !response.getMeta().isEnd();
        }

        Map<String, String> mdcContext = org.slf4j.MDC.getCopyOfContextMap();

        List<NearbySpotResponseDto> items = places.stream()
                .map(place -> CompletableFuture.supplyAsync(
                        () -> enrichPlace(place, mdcContext), placeExecutor))
                .toList()
                .stream()
                .map(CompletableFuture::join)
                .toList();

        return NearbySpotPageResponseDto.builder()
                .items(items)
                .nextCursor(hasNext ? encodeCursor(page + 1) : null)
                .hasNext(hasNext)
                .build();
    }

    private int parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) return 1;
        try {
            return Integer.parseInt(new String(Base64.getDecoder().decode(cursor)));
        } catch (Exception e) {
            return 1;
        }
    }

    private String encodeCursor(int page) {
        return Base64.getEncoder().encodeToString(String.valueOf(page).getBytes());
    }

    private NearbySpotResponseDto enrichPlace(
            KakaoLocalClient.KakaoPlace place, Map<String, String> mdcContext
    ) {
        // 자식 스레드에 MDC 컨텍스트 복원
        if (mdcContext != null) org.slf4j.MDC.setContextMap(mdcContext);

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("스레드 인터럽트 발생", e);
        }

        try {
            String naverQuery = extractRegion(place.getAddressName()) + " " + place.getPlaceName();

            NaverSearchClient.NaverBlogResult blogResult;
            try {
                blogResult = CompletableFuture
                        .supplyAsync(() -> naverSearchClient.searchBlog(naverQuery), naverExecutor)
                        .get(3, TimeUnit.SECONDS);
            } catch (TimeoutException | ExecutionException | InterruptedException e) {
                log.warn("[SpotService] Naver 검색 타임아웃 또는 실패 - place={}", place.getPlaceName());
                blogResult = new NaverSearchClient.NaverBlogResult(0, null);
            }

            String thumbnail = SpotCategoryImage.toImageUrl(place.getCategoryName(), ncpBaseUrl);

            return NearbySpotResponseDto.builder()
                    .placeName(place.getPlaceName())
                    .addressName(place.getAddressName())
                    .roadAddressName(place.getRoadAddressName())
                    .distance(place.getDistance())
                    .placeUrl(place.getPlaceUrl())
                    .blogReviewCount(blogResult.total())
                    .blogReviewLink(blogResult.link())
                    .thumbnailUrl(thumbnail)
                    .x(place.getX())
                    .y(place.getY())
                    .build();
        } finally {
            semaphore.release();
            org.slf4j.MDC.clear();
        }
    }

    private int parseDistance(String distance) {
        if (distance == null || distance.isBlank()) return Integer.MAX_VALUE;
        try {
            return Integer.parseInt(distance);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    // "서울특별시 강남구 역삼동 ..." → "강남구" 정도만 추출
    private String extractRegion(String addressName) {
        if (addressName == null || addressName.isBlank()) return "";
        String[] parts = addressName.split(" ");
        return parts.length >= 2 ? parts[1] : parts[0];
    }
}
