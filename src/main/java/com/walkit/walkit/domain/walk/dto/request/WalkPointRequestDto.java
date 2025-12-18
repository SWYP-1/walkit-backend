package com.walkit.walkit.domain.walk.dto.request;

import lombok.Getter;

@Getter
public class WalkPointRequestDto {
    private Double latitude;
    private Double longitude;

    private Long timestampMillis;
}