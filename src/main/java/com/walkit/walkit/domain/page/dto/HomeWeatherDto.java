package com.walkit.walkit.domain.page.dto;

import com.walkit.walkit.domain.weather.entity.PrecipType;
import com.walkit.walkit.domain.weather.entity.SkyStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeWeatherDto {

    private String imageName;
    private SkyStatus sky;
    private PrecipType precipType;
    private double temperature;
}
