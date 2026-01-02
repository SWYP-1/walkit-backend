package com.walkit.walkit.domain.weather.service;

import com.walkit.walkit.domain.weather.dto.CurrentWeatherResponseDto;
import com.walkit.walkit.domain.weather.dto.KmaItem;
import com.walkit.walkit.domain.weather.util.KmaBaseTime;
import com.walkit.walkit.domain.weather.util.KmaClient;
import com.walkit.walkit.domain.weather.util.KmaGridConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class WeatherService {

    private final KmaClient kmaClient;
    private final RedisTemplate<String, CurrentWeatherResponseDto> weatherRedisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    private static final Duration CACHE_TTL = Duration.ofHours(1);
    private static final Duration FAIL_TTL  = Duration.ofSeconds(30);;

    private static final Duration LOCK_TTL = Duration.ofSeconds(20);
    private static final int[] BACKOFF_MS = {100, 300, 600, 1000};


    public WeatherService(
            KmaClient kmaClient,
            RedisTemplate<String, CurrentWeatherResponseDto> weatherRedisTemplate,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.kmaClient = kmaClient;
        this.weatherRedisTemplate = weatherRedisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public CurrentWeatherResponseDto getCurrent(double lat, double lon) {
        // 위경도 -> 격자
        KmaGridConverter.Grid grid = KmaGridConverter.toGrid(lat, lon);

        String cKey = cacheKey(grid.nx(), grid.ny());
        String fKey = failKey(grid.nx(), grid.ny());
        String lKey = lockKey(grid.nx(), grid.ny());

        // 캐시 히트면 바로 반환
        CurrentWeatherResponseDto cached = weatherRedisTemplate.opsForValue().get(cKey);
        if (cached != null) return cached;

        // 최근 실패 캐시가 있으면 실패 처리
/*
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(fKey))) {
            throw new IllegalStateException("Weather provider temporarily unavailable");
        }
*/

        // 분산락 시도 (동일 nx,ny에 대해 한 요청만 외부호출)
        String token = UUID.randomUUID().toString();
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lKey, token, LOCK_TTL);

        if (Boolean.TRUE.equals(locked)) {
            try {
                // 락 체크
                CurrentWeatherResponseDto doubleCheck = weatherRedisTemplate.opsForValue().get(cKey);
                if (doubleCheck != null) return doubleCheck;

                ZonedDateTime nowKst = ZonedDateTime.now(KmaBaseTime.KST);
                KmaBaseTime.BaseDateTime ncstBase = KmaBaseTime.calcNcstBase(nowKst);
                KmaBaseTime.BaseDateTime fcstBase = KmaBaseTime.calcFcstBase(nowKst);

                List<KmaItem> ncstItems = kmaClient.getUltraSrtNcst(
                        ncstBase.baseDate(), ncstBase.baseTime(), grid.nx(), grid.ny()
                );

                List<KmaItem> fcstItems = kmaClient.getUltraSrtFcst(
                        fcstBase.baseDate(), fcstBase.baseTime(), grid.nx(), grid.ny()
                );

                CurrentWeatherResponseDto fresh =
                        KmaWeatherMapper.mergeToCurrentWeather(grid, nowKst, ncstItems, fcstItems);

                // 정상 캐시 저장 + TTL
                weatherRedisTemplate.opsForValue().set(cKey, fresh, CACHE_TTL);
                log.info("weather cached key={}", cKey);
                return fresh;

            } catch (Exception e) {
                // 실패 캐시 저장
                stringRedisTemplate.opsForValue().set(fKey, "1", FAIL_TTL);
                throw e;
            } finally {
                // 락 해제(토큰 매칭 후 삭제)
                releaseLockSafely(lKey, token);
            }
        }

        // 락 못 잡았으면 기다렸다가 캐시 재조회
        for (int backoff : BACKOFF_MS) {
            sleepMillis(backoff);
            CurrentWeatherResponseDto afterWait = weatherRedisTemplate.opsForValue().get(cKey);
            if (afterWait != null) return afterWait;
        }

        // 한 번 더 캐시 체크 후 실패 처리
        CurrentWeatherResponseDto last = weatherRedisTemplate.opsForValue().get(cKey);
        if (last != null) return last;
        throw new IllegalStateException("Weather cache miss after lock contention");
    }

    private String cacheKey(int nx, int ny) {
        return "weather:current:nx=" + nx + ":ny=" + ny;
    }


    private String failKey(int nx, int ny) {
        return "weather:current:fail:nx=" + nx + ":ny=" + ny;
    }

    private String lockKey(int nx, int ny) {
        return "lock:weather:current:nx=" + nx + ":ny=" + ny;
    }

    private void sleepMillis(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void releaseLockSafely(String lockKey, String token) {
        // 값이 토큰일 때만 삭제
        String val = stringRedisTemplate.opsForValue().get(lockKey);
        if (Objects.equals(val, token)) {
            stringRedisTemplate.delete(lockKey);
        }
    }
}