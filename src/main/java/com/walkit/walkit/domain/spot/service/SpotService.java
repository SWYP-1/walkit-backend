package com.walkit.walkit.domain.spot.service;

import com.walkit.walkit.domain.spot.client.KakaoLocalClient;
import com.walkit.walkit.domain.spot.client.NaverSearchClient;
import com.walkit.walkit.domain.spot.dto.response.NearbySpotResponseDto;
import com.walkit.walkit.domain.spot.enums.SpotCategoryImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

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

    public List<NearbySpotResponseDto> getNearbySpots(
            String query, double x, double y, int radius, int size, String sort
    ) {
        List<KakaoLocalClient.KakaoPlace> places;

        if (query == null || query.isBlank()) {
            List<CompletableFuture<List<KakaoLocalClient.KakaoPlace>>> categoryFutures =
                    Arrays.stream(SpotCategoryImage.values())
                            .map(cat -> CompletableFuture.supplyAsync(
                                    () -> kakaoLocalClient.searchCategory(cat.getKakaoGroupCode(), x, y, radius, size, sort),
                                    placeExecutor))
                            .toList();

            places = categoryFutures.stream()
                    .flatMap(f -> f.join().stream())
                    .distinct()
                    .limit(size)
                    .toList();
        } else {
            places = kakaoLocalClient.searchKeyword(query, x, y, radius, size, sort);
        }

        // MDC 컨텍스트를 자식 스레드에 전파
        Map<String, String> mdcContext = org.slf4j.MDC.getCopyOfContextMap();

        // 장소별로 CompletableFuture 생성 (병렬 처리)
        List<CompletableFuture<NearbySpotResponseDto>> futures = places.stream()
                .map(place -> CompletableFuture.supplyAsync(
                        () -> enrichPlace(place, mdcContext), placeExecutor))
                .toList();

        // 전체 완료 대기 후 결과 수집
        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
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

            NaverSearchClient.NaverBlogResult blogResult =
                    CompletableFuture.supplyAsync(() -> naverSearchClient.searchBlog(naverQuery), naverExecutor).join();

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

    // "서울특별시 강남구 역삼동 ..." → "강남구" 정도만 추출
    private String extractRegion(String addressName) {
        if (addressName == null || addressName.isBlank()) return "";
        String[] parts = addressName.split(" ");
        return parts.length >= 2 ? parts[1] : parts[0];
    }
}
