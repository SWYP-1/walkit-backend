package com.walkit.walkit.domain.map.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowerWalkingRecordResponseDto {

    private Long userId;
    private Long walkId;
    private Double latitude;
    private Double longitude;
    private MapCharacterDto responseCharacterDto;
}
