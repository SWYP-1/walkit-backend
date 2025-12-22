package com.walkit.walkit.domain.page.dto;

import com.walkit.walkit.domain.character.dto.response.ResponseCharacterDto;
import com.walkit.walkit.domain.mission.dto.WeeklyMissionResponseDto;
import com.walkit.walkit.domain.walk.dto.response.WalkResponseDto;
import com.walkit.walkit.domain.weather.dto.CurrentWeatherResponseDto;
import lombok.Builder;

@Builder
public class ResponseHomeDto {

    private ResponseCharacterDto characterDto;
    private String walkProgressPercentage;

    // 누적걸음
    private int todaySteps;

    // 날씨
    private CurrentWeatherResponseDto weatherDto;

    // 추천미션
    private WeeklyMissionResponseDto weeklyMissionDto;

    // 나의 산책기록 목록
    private WalkResponseDto walkResponseDto;
}
