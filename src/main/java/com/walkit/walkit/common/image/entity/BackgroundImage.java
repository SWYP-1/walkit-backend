package com.walkit.walkit.common.image.entity;

import com.walkit.walkit.common.image.enums.Season;
import com.walkit.walkit.common.image.enums.Weather;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class BackgroundImage extends Image {

    @Enumerated(EnumType.STRING)
    private Season season;

    @Enumerated(EnumType.STRING)
    private Weather weather;

    private boolean isLong;

    @Builder
    public BackgroundImage(Season season, Weather weather, boolean isLong) {
        this.season = season;
        this.weather = weather;
        this.isLong = isLong;
    }
}
