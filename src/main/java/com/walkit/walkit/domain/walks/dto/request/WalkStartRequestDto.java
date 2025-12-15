package com.walkit.walkit.domain.walks.dto.request;

import com.walkit.walkit.domain.walks.entity.Emotion;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WalkStartRequestDto {
    private Long startTime;               // 산책 시작 시간
    private Emotion preWalkEmotion;       // 산책 시작 감정
}
