package com.walkit.walkit.domain.walks.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WalkPointResponseDto {
    private Double latitude;
    private Double longitude;
    private Long timestampMillis;
}
