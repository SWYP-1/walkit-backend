package com.walkit.walkit.domain.weather.util;


import java.time.*;
import java.time.format.DateTimeFormatter;

public class KmaBaseTime {

    private KmaBaseTime() {}

    public static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HHmm");

    public record BaseDateTime(String baseDate, String baseTime) {}

    /**
     * 초단기실황: 실황은 반영 지연이 있어서 "현재시각 - 40분"을 안전 기준으로 잡는 경우가 많음.
     * => 그 시각을 '정시(HH00)'로 내림
     */
    public static BaseDateTime calcNcstBase(ZonedDateTime nowKst) {
        ZonedDateTime safe = nowKst.minusMinutes(40);
        ZonedDateTime floored = safe.withMinute(0).withSecond(0).withNano(0);
        return new BaseDateTime(floored.format(DATE), floored.format(TIME));
    }

    /**
     * 초단기예보: 보통 30분 단위 발표. 안전하게 "현재시각 - 45분" 후 30분 단위로 내림.
     * => HH00 or HH30
     */
    public static BaseDateTime calcFcstBase(ZonedDateTime nowKst) {
        ZonedDateTime safe = nowKst.minusMinutes(45);
        int minute = safe.getMinute();
        int flooredMinute = (minute < 30) ? 0 : 30;
        ZonedDateTime floored = safe.withMinute(flooredMinute).withSecond(0).withNano(0);
        return new BaseDateTime(floored.format(DATE), floored.format(TIME));
    }
}

