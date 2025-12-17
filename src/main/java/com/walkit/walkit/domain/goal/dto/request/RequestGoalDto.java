package com.walkit.walkit.domain.goal.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class RequestGoalDto {

    @Min(value = 1000, message = "목표 걸음 수는 최소 1,000보 이상이어야 합니다.")
    @Max(value = 100000, message = "목표 걸음 수는 최대 100,000보까지 설정 가능합니다.")
    private int targetStepCount;

    @Min(value = 1, message = "목표 산책 횟수는 최소 1회 이상이어야 합니다.")
    @Max(value = 7, message = "목표 산책 횟수는 최대 7회까지 설정 가능합니다.")
    private int targetWalkCount;
}
