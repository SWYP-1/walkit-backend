package com.walkit.walkit.domain.page.dto;

import com.walkit.walkit.domain.character.dto.response.ResponseCharacterDto;
import lombok.Builder;

@Builder
public class ResponseHomeDto {

    private ResponseCharacterDto characterDto;
    private String walkProgressPercentage;

    // 누적걸음

    // 날씨, 온도

    // 추천미션

    // 나의 산책기록 목록
}
