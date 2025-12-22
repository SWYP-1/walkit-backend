package com.walkit.walkit.domain.weather.controller;


import com.walkit.walkit.domain.weather.dto.CurrentWeatherResponseDto;
import com.walkit.walkit.domain.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Weather", description = "날씨 조회 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/current")
    public ResponseEntity<CurrentWeatherResponseDto> current(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon
    ) {
        return ResponseEntity.ok(weatherService.getCurrent(lat, lon));
    }

}
