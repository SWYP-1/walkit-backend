package com.walkit.walkit.domain.walks.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.walkit.walkit.domain.walks.entity.Emotion;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class WalkRequestDto {

    private Emotion preWalkEmotion;     // 시작 감정
    private Emotion postWalkEmotion;    // 종료  감정
    private String note;                // 산책 일기 텍스트
    private List<WalkPointRequestDto> points;    // 코스
    @JsonIgnore
    private MultipartFile image;    // 이미지
    private Long endTime;           // 산책 종료 시간
    private Long startTime;         // 산책 시작 시간
    private Double totalDistance;   // 총 산책 거리
    private Integer stepCount;      // 걸음 수

}
