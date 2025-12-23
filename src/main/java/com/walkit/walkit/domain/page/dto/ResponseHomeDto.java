package com.walkit.walkit.domain.page.dto;

import com.walkit.walkit.domain.character.dto.response.ResponseCharacterDto;
import com.walkit.walkit.domain.mission.dto.WeeklyMissionResponseDto;
import com.walkit.walkit.domain.walk.dto.response.WalkResponseDto;
import com.walkit.walkit.domain.weather.dto.CurrentWeatherResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResponseHomeDto {

    private ResponseCharacterDto characterDto;
    private String walkProgressPercentage;
    private int todaySteps;                            // 누적걸음
    private CurrentWeatherResponseDto weatherDto;      // 날씨
    private WeeklyMissionResponseDto weeklyMissionDto; // 추천미션
    private List<WalkResponseDto> walkResponseDto;     // 나의 산책기록
}
