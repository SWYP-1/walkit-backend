package com.walkit.walkit.domain.weather.dto;

import com.walkit.walkit.domain.weather.entity.PrecipType;
import com.walkit.walkit.domain.weather.entity.SkyStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CurrentWeatherResponseDto {
    int nx;
    int ny;
    ZonedDateTime generatedAt;   // 서버 생성 시각
    double tempC;                // T1H
    double rain1hMm;             // RN1 (없으면 0)
    PrecipType precipType;       // PTY
    SkyStatus sky;               // SKY
}
