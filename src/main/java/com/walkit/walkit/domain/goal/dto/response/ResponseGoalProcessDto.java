package com.walkit.walkit.domain.goal.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseGoalProcessDto {

    private int currentWalkCount;
    private String walkProgressPercentage;
}
