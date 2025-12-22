package com.walkit.walkit.domain.walk.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WalkTotalSummaryResponseDto {
    private long totalWalkCount;
    private long totalWalkTimeMillis;
}
