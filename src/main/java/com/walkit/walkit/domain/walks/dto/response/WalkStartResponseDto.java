package com.walkit.walkit.domain.walks.dto.response;

import com.walkit.walkit.domain.walks.entity.Emotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalkStartResponseDto {
    private Long id;
    private Emotion preWalkEmotion;
    private Long startTime;
    private LocalDateTime createdDate;
}
