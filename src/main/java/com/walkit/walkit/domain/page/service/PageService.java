package com.walkit.walkit.domain.page.service;

import com.walkit.walkit.domain.character.dto.response.ResponseCharacterDto;
import com.walkit.walkit.domain.character.service.CharacterService;
import com.walkit.walkit.domain.goal.dto.response.ResponseGoalProcessDto;
import com.walkit.walkit.domain.goal.service.GoalService;
import com.walkit.walkit.domain.mission.dto.WeeklyMissionResponseDto;
import com.walkit.walkit.domain.mission.service.WeeklyMissionService;
import com.walkit.walkit.domain.page.dto.ResponseHomeDto;
import com.walkit.walkit.domain.page.enums.HomeWeather;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.domain.walk.dto.response.WalkResponseDto;
import com.walkit.walkit.domain.walk.service.WalkService;
import com.walkit.walkit.domain.weather.dto.CurrentWeatherResponseDto;
import com.walkit.walkit.domain.weather.entity.PrecipType;
import com.walkit.walkit.domain.weather.entity.SkyStatus;
import com.walkit.walkit.domain.weather.service.WeatherService;
import com.walkit.walkit.global.exception.CustomException;
import com.walkit.walkit.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PageService {

    private final UserRepository userRepository;
    private final CharacterService characterService;
    private final GoalService goalService;
    private final WalkService walkService;
    private final WeatherService weatherService;
    private final WeeklyMissionService weeklyMissionService;


    public ResponseHomeDto getHomePage(Long userId, double lat, double lon) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ResponseCharacterDto characterDto = findCharacterDto(user, lat, lon);
        String walkProgressPercentage = goalService.findGoalProcess(user.getId()).getWalkProgressPercentage();

        int todaySteps = walkService.getTodayStepCount(userId);
        WeeklyMissionResponseDto weeklyMissionDto = weeklyMissionService.getEnsureAssignedForThisWeek(userId);

        List<WalkResponseDto> walkResponseDto = walkService.getRecentWalks(userId);

        CurrentWeatherResponseDto weatherDto = null;
        HomeWeather homeWeather = HomeWeather.SUNNY;

        try {
            weatherDto = weatherService.getCurrent(lat, lon);
            if (weatherDto.getPrecipType() == PrecipType.RAIN) {
                homeWeather = HomeWeather.RAIN;
            } else if (weatherDto.getPrecipType() == PrecipType.SNOW) {
                homeWeather = HomeWeather.SNOW;
            } else if (weatherDto.getSky() == SkyStatus.OVERCAST) {
                homeWeather = HomeWeather.OVERCAST;
            }
        } catch (Exception e) {
            log.info("기상청 오류");
        }

        return ResponseHomeDto.builder()
                .characterDto(characterDto)
                .walkProgressPercentage(walkProgressPercentage)
                .todaySteps(todaySteps)
                .temperature(weatherDto == null ? null : weatherDto.getTempC())
                .weather(homeWeather)
                .weeklyMissionDto(weeklyMissionDto)
                .walkResponseDto(walkResponseDto)
                .build();

    }

    private ResponseCharacterDto findCharacterDto(User user, double lat, double lon) {
        return characterService.find(user.getId(), lat, lon);
    }
}
