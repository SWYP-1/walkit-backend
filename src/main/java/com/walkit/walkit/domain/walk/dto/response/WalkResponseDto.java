package com.walkit.walkit.domain.walk.dto.response;

import com.walkit.walkit.domain.walk.entity.Emotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalkResponseDto {
    private Long id;
    private Emotion preWalkEmotion;
    private Emotion postWalkEmotion;
    private String note;
    private String imageUrl;
    private Long startTime;
    private Long endTime;
    private Long totalTime;
    private Integer stepCount;
    private Double totalDistance;
    private LocalDateTime createdDate;
    private List<WalkPointResponseDto> points;
}
