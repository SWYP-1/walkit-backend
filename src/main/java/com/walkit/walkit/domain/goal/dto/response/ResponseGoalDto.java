package com.walkit.walkit.domain.goal.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseGoalDto {

    private int targetSteps;
    private int targetWalks;
}
