package com.walkit.walkit.domain.page.dto;

import com.walkit.walkit.domain.character.dto.response.ResponseCharacterDto;
import com.walkit.walkit.domain.weather.entity.PrecipType;
import com.walkit.walkit.domain.weather.entity.SkyStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeDto {

    private ResponseCharacterDto responseCharacterDto;
    private SkyStatus sky;
    private PrecipType precipType;
    private Double temperature;

    public static HomeDto from(ResponseCharacterDto characterDto, HomeWeatherDto homeWeatherDto) {
        return HomeDto.builder()
                .responseCharacterDto(characterDto)
                .sky(homeWeatherDto != null ? homeWeatherDto.getSky() : null)
                .precipType(homeWeatherDto != null ? homeWeatherDto.getPrecipType() : null)
                .temperature(homeWeatherDto != null ? homeWeatherDto.getTemperature() : null)
                .build();
    }
}
