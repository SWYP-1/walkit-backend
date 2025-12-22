package com.walkit.walkit.domain.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@JsonIgnoreProperties(ignoreUnknown = true)
public record KmaItem(
        String baseDate,
        String baseTime,
        String category,
        String nx,
        String ny,
        String obsrValue,  // NCST
        String fcstDate,   // FCST
        String fcstTime,   // FCST
        String fcstValue   // FCST
){}
