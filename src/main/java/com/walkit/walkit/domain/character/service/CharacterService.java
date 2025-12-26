package com.walkit.walkit.domain.character.service;

import com.walkit.walkit.common.image.entity.CharacterWearImage;
import com.walkit.walkit.common.image.enums.Season;
import com.walkit.walkit.common.image.enums.Weather;
import com.walkit.walkit.common.image.repository.BackgroundImageRepository;
import com.walkit.walkit.common.image.repository.CharacterImageRepository;
import com.walkit.walkit.common.image.repository.CharacterWearImageRepository;
import com.walkit.walkit.common.image.repository.ImageRepository;
import com.walkit.walkit.domain.item.dto.request.RequestItemWearDto;
import com.walkit.walkit.domain.character.dto.response.ResponseCharacterDto;
import com.walkit.walkit.domain.item.entity.Item;
import com.walkit.walkit.domain.item.entity.ItemManagement;
import com.walkit.walkit.domain.character.entity.Character;
import com.walkit.walkit.domain.character.entity.CharacterWear;
import com.walkit.walkit.domain.item.enums.Position;
import com.walkit.walkit.domain.item.repository.ItemManagementRepository;
import com.walkit.walkit.domain.item.repository.ItemRepository;
import com.walkit.walkit.domain.character.repository.CharacterWearRepository;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.domain.weather.entity.PrecipType;
import com.walkit.walkit.domain.weather.entity.SkyStatus;
import com.walkit.walkit.domain.weather.service.WeatherService;
import com.walkit.walkit.global.exception.CustomException;
import com.walkit.walkit.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CharacterService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemManagementRepository itemManagementRepository;
    private final CharacterWearRepository characterWearRepository;
    private final WeatherService weatherService;
    private final ImageRepository imageRepository;
    private final BackgroundImageRepository backgroundImageRepository;
    private final CharacterImageRepository characterImageRepository;
    private final CharacterWearImageRepository characterWearImageRepository;

    public void init(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Character character = new Character();

        user.initCharacter(character);
    }

    public ResponseCharacterDto find(Long userId, double lat, double lon) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Character character = user.getCharacter();

        String backgroundImage = findNotLongBackGroundImage(lat, lon);
        String characterImage = characterImageRepository.findByGrade(character.getGrade()).getImageName();

        log.info("background image is {}", backgroundImage);

        return ResponseCharacterDto.from(character, user, characterImage, backgroundImage);
    }

    public ResponseCharacterDto findWalkCharacter(Long userId, double lat, double lon) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Character character = user.getCharacter();

        String backgroundImage = findLongBackGroundImage(lat, lon);
        String characterImage = characterImageRepository.findByGrade(character.getGrade()).getImageName();

        log.info("background image is {}", backgroundImage);

        return ResponseCharacterDto.from(character, user, characterImage, backgroundImage);
    }

    public void wearOrTakeOff(Long userId, Long itemId, RequestItemWearDto dto) {
        if (dto.isWorn()) {
            wear(userId, itemId);
        } else {
            takeOff(userId, itemId);
        }
    }

    private void wear(Long userId, Long itemId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

        checkUserOwnItem(user, item);
        checkAlreadyWearSamePosition(user, item);

        wearItem(user, item, user.getCharacter());

        saveCharacterWear(user, item);
    }

    private void takeOff(Long userId, Long itemId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

        checkUserOwnItem(user, item);

        takeOffItem(user, item);

        deleteCharacterWear(user, item);
    }

    private void checkAlreadyWearSamePosition(User user, Item item) {
        Position newItemPosition = item.getPosition();
        Character character = user.getCharacter();

        CharacterWear wearToRemove = character.getCharacterWears().stream()
                .filter(characterWear -> characterWear.getItem().getPosition() == newItemPosition)
                .findFirst()
                .orElse(null);

        if (wearToRemove != null) {
            log.info("characterWearId : {}", wearToRemove.getId());
            Item findItem = wearToRemove.getItem();

            character.removeCharacterWear(wearToRemove);

            ItemManagement itemManagement = itemManagementRepository.findByUserAndItem(user, findItem)
                    .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));
            itemManagement.inActive();
        }
    }

    private String findNotLongBackGroundImage(double lat, double lon) {
        SkyStatus sky = weatherService.getCurrent(lat, lon).getSky();
        PrecipType precipType = weatherService.getCurrent(lat, lon).getPrecipType();

        int month = LocalDate.now().getMonthValue();

        if (month >= 3 && month <= 5) { // 봄
            if (sky == SkyStatus.SUNNY) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.SPRING, Weather.SUNNY, false).getImageName();
            } else if (precipType == PrecipType.RAIN) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.SPRING, Weather.RAINY, false).getImageName();
            } else if (sky == SkyStatus.OVERCAST) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.SPRING, Weather.OVERCAST, false).getImageName();
            } else if (sky == SkyStatus.CLOUDY_MANY) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.SPRING, Weather.OVERCAST, false).getImageName();
            } else {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.SPRING, Weather.SUNNY, false).getImageName();
            }
        } else if (month >= 6 && month <= 8) { // 여름
            if (sky == SkyStatus.SUNNY) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.SUMMER, Weather.SUNNY, false).getImageName();
            } else if (precipType == PrecipType.RAIN) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.SUMMER, Weather.RAINY, false).getImageName();
            } else if (sky == SkyStatus.OVERCAST) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.SUMMER, Weather.OVERCAST, false).getImageName();
            } else if (sky == SkyStatus.CLOUDY_MANY) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.SUMMER, Weather.OVERCAST, false).getImageName();
            } else {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.SUMMER, Weather.SUNNY, false).getImageName();
            }
        } else if (month >= 9 && month <= 11) { // 가을
            if (sky == SkyStatus.SUNNY) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.FALL, Weather.SUNNY, false).getImageName();
            } else if (precipType == PrecipType.RAIN) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.FALL, Weather.RAINY, false).getImageName();
            } else if (sky == SkyStatus.OVERCAST) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.FALL, Weather.OVERCAST, false).getImageName();
            } else if (sky == SkyStatus.CLOUDY_MANY) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.FALL, Weather.OVERCAST, false).getImageName();
            } else {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.FALL, Weather.SUNNY, false).getImageName();
            }
        } else { // 겨울
            if (sky == SkyStatus.SUNNY) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.WINTER, Weather.SUNNY, false).getImageName();
            } else if (precipType == PrecipType.RAIN) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.WINTER, Weather.RAINY, false).getImageName();
            } else if (sky == SkyStatus.OVERCAST) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.WINTER, Weather.OVERCAST, false).getImageName();
            }  else if (precipType == PrecipType.SNOW) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.WINTER, Weather.SNOWY, false).getImageName();
            } else if (sky == SkyStatus.CLOUDY_MANY) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.WINTER, Weather.OVERCAST, false).getImageName();
            } else {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.WINTER, Weather.SUNNY, false).getImageName();
            }
        }
    }

    private String findLongBackGroundImage(double lat, double lon) {
        SkyStatus sky = weatherService.getCurrent(lat, lon).getSky();
        PrecipType precipType = weatherService.getCurrent(lat, lon).getPrecipType();

        int month = LocalDate.now().getMonthValue();

        if (month >= 3 && month <= 5) { // 봄
            if (sky == SkyStatus.SUNNY) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.SPRING, Weather.SUNNY, true).getImageName();
            } else if (precipType == PrecipType.RAIN) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.SPRING, Weather.RAINY, true).getImageName();
            } else if (sky == SkyStatus.OVERCAST) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.SPRING, Weather.OVERCAST, true).getImageName();
            }
        } else if (month >= 6 && month <= 8) { // 여름
            if (sky == SkyStatus.SUNNY) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.SUMMER, Weather.SUNNY, true).getImageName();
            } else if (precipType == PrecipType.RAIN) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.SUMMER, Weather.RAINY, true).getImageName();
            } else if (sky == SkyStatus.OVERCAST) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.SUMMER, Weather.OVERCAST, true).getImageName();
            }
        } else if (month >= 9 && month <= 11) { // 가을
            if (sky == SkyStatus.SUNNY) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.FALL, Weather.SUNNY, true).getImageName();
            } else if (precipType == PrecipType.RAIN) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.FALL, Weather.RAINY, true).getImageName();
            } else if (sky == SkyStatus.OVERCAST) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.FALL, Weather.OVERCAST, true).getImageName();
            }
        } else { // 겨울
            if (sky == SkyStatus.SUNNY) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.WINTER, Weather.SUNNY, true).getImageName();
            } else if (precipType == PrecipType.RAIN) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.WINTER, Weather.RAINY, true).getImageName();
            } else if (sky == SkyStatus.OVERCAST) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.WINTER, Weather.OVERCAST, true).getImageName();
            } else if (precipType == PrecipType.SNOW) {
                return backgroundImageRepository.findBySeasonAndWeatherAndIsLong(Season.WINTER, Weather.SNOWY, true).getImageName();
            }

        }


        return null;
    }



    private void wearItem(User user, Item item, Character character) {
        ItemManagement itemManagement = itemManagementRepository.findByUserAndItem(user, item).orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_OWNED));
        itemManagement.active();

        CharacterWearImage characterWearImage = characterWearImageRepository.findByPositionAndGradeAndItemName(item.getPosition(), character.getGrade(), item.getItemName());

        character.updateImage(characterWearImage);
    }

    private void saveCharacterWear(User user, Item item) {
        Character character = user.getCharacter();
        CharacterWear characterWear = CharacterWear.from(character, item);
        characterWearRepository.save(characterWear);
    }

    private void checkUserOwnItem(User user, Item item) {
        if (!itemManagementRepository.existsByUserAndItem(user, item)) {
            throw new CustomException(ErrorCode.ITEM_NOT_OWNED);
        }
    }

    private void deleteCharacterWear(User user, Item item) {
        Character character = user.getCharacter();
        characterWearRepository.deleteByCharacterAndItem(character, item);
    }

    private void takeOffItem(User user, Item item) {
        ItemManagement itemManagement = itemManagementRepository.findByUserAndItem(user, item).orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_OWNED));
        itemManagement.inActive();

        Character character = user.getCharacter();
        character.updateImageToNull(item);
    }
}
