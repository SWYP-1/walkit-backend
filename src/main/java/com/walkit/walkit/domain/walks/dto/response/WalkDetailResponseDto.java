package com.walkit.walkit.domain.walks.dto.response;

import com.walkit.walkit.domain.walks.entity.Emotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalkDetailResponseDto {
    private Long id;
    private Emotion preWalkEmotion;
    private Emotion postWalkEmotion;
    private String note;
    private String imageUrl;
    private Long startTime;
    private Long endTime;
    private Integer stepCount;
    private Double totalDistance;
    private LocalDateTime createdDate;
    private List<WalkPointResponseDto> points;
}
