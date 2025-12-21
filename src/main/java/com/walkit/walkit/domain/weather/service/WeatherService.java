package com.walkit.walkit.domain.weather.service;

import com.walkit.walkit.domain.weather.dto.CurrentWeatherResponseDto;
import com.walkit.walkit.domain.weather.dto.KmaItem;
import com.walkit.walkit.domain.weather.util.KmaBaseTime;
import com.walkit.walkit.domain.weather.util.KmaClient;
import com.walkit.walkit.domain.weather.util.KmaGridConverter;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class WeatherService {

    private final KmaClient kmaClient;

    public WeatherService(KmaClient kmaClient) {
        this.kmaClient = kmaClient;
    }

    public CurrentWeatherResponseDto getCurrent(double lat, double lon) {
        // 1) 위경도 -> 격자
        KmaGridConverter.Grid grid = KmaGridConverter.toGrid(lat, lon);

        // 2) base_date/base_time 계산 (KST 기준)
        ZonedDateTime nowKst = ZonedDateTime.now(KmaBaseTime.KST);
        KmaBaseTime.BaseDateTime ncstBase = KmaBaseTime.calcNcstBase(nowKst);
        KmaBaseTime.BaseDateTime fcstBase = KmaBaseTime.calcFcstBase(nowKst);

        // 3) 초단기실황 (온도 등 현재값)
        List<KmaItem> ncstItems = kmaClient.getUltraSrtNcst(
                ncstBase.baseDate(), ncstBase.baseTime(), grid.nx(), grid.ny()
        );

        // 4) 초단기예보 (PTY/SKY는 예보 쪽이 더 안정적)
        List<KmaItem> fcstItems = kmaClient.getUltraSrtFcst(
                fcstBase.baseDate(), fcstBase.baseTime(), grid.nx(), grid.ny()
        );

        // 5) 필요한 값 뽑고 매핑
        return KmaWeatherMapper.mergeToCurrentWeather(grid, nowKst, ncstItems, fcstItems);
    }
}
