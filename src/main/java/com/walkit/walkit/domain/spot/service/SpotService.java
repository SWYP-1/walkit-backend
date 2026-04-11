package com.walkit.walkit.domain.spot.service;

import com.walkit.walkit.domain.spot.client.KakaoLocalClient;
import com.walkit.walkit.domain.spot.client.NaverSearchClient;
import com.walkit.walkit.domain.spot.dto.response.NearbySpotResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpotService {

    private final KakaoLocalClient kakaoLocalClient;
    private final NaverSearchClient naverSearchClient;

    public List<NearbySpotResponseDto> getNearbySpots(
            String query, double x, double y, int radius, int size, String sort
    ) {
        List<KakaoLocalClient.KakaoPlace> places =
                kakaoLocalClient.searchKeyword(query, x, y, radius, size, sort);

        return places.stream()
                .map(place -> {
                    String region = extractRegion(place.getAddressName());
                    String naverQuery = region + " " + place.getPlaceName();

                    NaverSearchClient.NaverBlogResult blogResult =
                            naverSearchClient.searchBlog(naverQuery);
                    String thumbnail =
                            naverSearchClient.searchImage(naverQuery);

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
                })
                .toList();
    }

    // "서울특별시 강남구 역삼동 ..." → "강남구" 정도만 추출
    private String extractRegion(String addressName) {
        if (addressName == null || addressName.isBlank()) return "";
        String[] parts = addressName.split(" ");
        return parts.length >= 2 ? parts[1] : parts[0];
    }
}
