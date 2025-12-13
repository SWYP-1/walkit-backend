package com.walkit.walkit.domain.walks.dto.request;

import lombok.Getter;

@Getter
public class WalkPointRequestDto {
    private Double latitude;
    private Double longitude;

    private Long timestampMillis;
}