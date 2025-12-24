package com.walkit.walkit.domain.walk.dto.response;

import com.walkit.walkit.domain.walk.entity.Emotion;
import com.walkit.walkit.domain.walk.entity.Walk;
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

    public static WalkResponseDto fromDetail(Walk walk) {
        return WalkResponseDto.builder()
                .id(walk.getId())
                .preWalkEmotion(walk.getPreWalkEmotion())
                .postWalkEmotion(walk.getPostWalkEmotion())
                .note(walk.getNote())
                .stepCount(walk.getStepCount())
                .totalDistance(walk.getTotalDistance())
                .startTime(walk.getStartTime())
                .endTime(walk.getEndTime())
                .totalTime(walk.getTotalTime())
                .imageUrl(walk.getImageUrl())
                .createdDate(walk.getCreatedDate())
                .points(
                        walk.getPoints().stream()
                                .map(p -> new WalkPointResponseDto(
                                        p.getLatitude(),
                                        p.getLongitude(),
                                        p.getRecordedAt()
                                ))
                                .toList()
                )
                .build();
    }
}
