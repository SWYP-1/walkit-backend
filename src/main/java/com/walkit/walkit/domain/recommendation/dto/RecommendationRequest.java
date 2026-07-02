package com.walkit.walkit.domain.recommendation.dto;

import com.walkit.walkit.domain.recommendation.model.Emotion;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecommendationRequest {

    @NotNull(message = "감정은 필수입니다")
    private Emotion emotion;

    @NotNull(message = "위도는 필수입니다")
    @DecimalMin(value = "33.0", message = "위도는 33.0 이상이어야 합니다")
    @DecimalMax(value = "39.0", message = "위도는 39.0 이하이어야 합니다")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다")
    @DecimalMin(value = "124.0", message = "경도는 124.0 이상이어야 합니다")
    @DecimalMax(value = "132.0", message = "경도는 132.0 이하이어야 합니다")
    private Double longitude;

    @NotNull(message = "원하는 소요시간은 필수입니다")
    @Min(value = 10, message = "소요시간은 최소 10분입니다")
    @Max(value = 120, message = "소요시간은 최대 120분입니다")
    private Integer desiredMinutes;

    @DecimalMin(value = "1.0", message = "반경은 최소 1km입니다")
    @DecimalMax(value = "10.0", message = "반경은 최대 10km입니다")
    private Double radiusKm = 5.0;
}
