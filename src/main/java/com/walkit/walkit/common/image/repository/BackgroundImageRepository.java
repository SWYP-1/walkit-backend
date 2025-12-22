package com.walkit.walkit.common.image.repository;

import com.walkit.walkit.common.image.entity.BackgroundImage;
import com.walkit.walkit.common.image.enums.Season;
import com.walkit.walkit.common.image.enums.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BackgroundImageRepository extends JpaRepository<BackgroundImage, Long> {
    List<BackgroundImage> findByWeather(Weather weather);

    BackgroundImage findBySeasonAndWeather(Season season, Weather weather);

    BackgroundImage findBySeasonAndWeatherAndIsLong(Season season, Weather weather, boolean isLong);
}
