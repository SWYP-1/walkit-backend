package com.walkit.walkit.domain.spot.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NearbySpotPageResponseDto {
    private List<NearbySpotResponseDto> items;
    private String nextCursor;
    private boolean hasNext;
}
