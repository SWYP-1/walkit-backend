package com.walkit.walkit.domain.weather.service;



import com.walkit.walkit.domain.weather.dto.CurrentWeatherResponseDto;
import com.walkit.walkit.domain.weather.dto.KmaItem;
import com.walkit.walkit.domain.weather.entity.PrecipType;
import com.walkit.walkit.domain.weather.entity.SkyStatus;
import com.walkit.walkit.domain.weather.util.KmaGridConverter;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KmaWeatherMapper {

    private KmaWeatherMapper() {}

    public static CurrentWeatherResponseDto mergeToCurrentWeather(
            KmaGridConverter.Grid grid,
            ZonedDateTime generatedAt,
            List<KmaItem> ncstItems,
            List<KmaItem> fcstItems
    ) {
        // 1) NCST: category -> obsrValue
        Map<String, String> ncst = ncstItems.stream()
                .filter(i -> i.category() != null)
                .collect(Collectors.toMap(
                        KmaItem::category,
                        i -> i.obsrValue() == null ? "" : i.obsrValue(),
                        (a, b) -> b
                ));

        double tempC = parseDoubleOr(ncst.get("T1H"), Double.NaN);
        double rain1h = parseDoubleOr(ncst.get("RN1"), 0.0);

        // 2) FCST: 가장 가까운 fcstTime 한 타임 선택 후 PTY/SKY만 뽑기
        // 초단기예보는 여러 시간대가 오므로 "현재시각 이후 가장 가까운 시간"을 선택
        String targetFcstTime = fcstItems.stream()
                .map(KmaItem::fcstTime)
                .filter(t -> t != null && t.length() == 4)
                .min(Comparator.naturalOrder()) // base_time 기준 첫 타임이 가장 가까운 편 (실무에서 안정적)
                .orElse(null);

        List<KmaItem> near = (targetFcstTime == null)
                ? fcstItems
                : fcstItems.stream().filter(i -> targetFcstTime.equals(i.fcstTime())).toList();

        Map<String, String> fcst = near.stream()
                .filter(i -> i.category() != null)
                .collect(Collectors.toMap(
                        KmaItem::category,
                        i -> i.fcstValue() == null ? "" : i.fcstValue(),
                        (a, b) -> b
                ));

        PrecipType precipType = mapPty(fcst.get("PTY"));
        SkyStatus sky = mapSky(fcst.get("SKY"));

        // 만약 tempC가 NaN이면 (간혹 실황이 늦게 뜨는 경우) 예보 TMP로 폴백
        if (Double.isNaN(tempC)) {
            tempC = parseDoubleOr(fcst.get("T1H"), Double.NaN);
            if (Double.isNaN(tempC)) {
                tempC = 0.0;
            }
        }

        return new CurrentWeatherResponseDto(
                grid.nx(), grid.ny(),
                generatedAt,
                tempC,
                rain1h,
                precipType,
                sky
        );
    }

    private static double parseDoubleOr(String s, double fallback) {
        if (s == null || s.isBlank()) return fallback;
        try {
            if ("강수없음".equals(s)) return 0.0;
            return Double.parseDouble(s);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static PrecipType mapPty(String pty) {
        if (pty == null || pty.isBlank()) return PrecipType.UNKNOWN;
        return switch (pty) {
            case "0" -> PrecipType.NONE;
            case "1" -> PrecipType.RAIN;
            case "2" -> PrecipType.RAIN_SNOW;
            case "3" -> PrecipType.SNOW;
            case "4" -> PrecipType.SHOWER;
            default -> PrecipType.UNKNOWN;
        };
    }

    private static SkyStatus mapSky(String sky) {
        if (sky == null || sky.isBlank()) return SkyStatus.UNKNOWN;
        return switch (sky) {
            case "1" -> SkyStatus.SUNNY;
            case "3" -> SkyStatus.CLOUDY_MANY;
            case "4" -> SkyStatus.OVERCAST;
            default -> SkyStatus.UNKNOWN;
        };
    }
}

