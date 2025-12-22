package com.walkit.walkit.domain.page.service;

import com.walkit.walkit.domain.character.dto.response.ResponseCharacterDto;
import com.walkit.walkit.domain.character.service.CharacterService;
import com.walkit.walkit.domain.goal.dto.response.ResponseGoalProcessDto;
import com.walkit.walkit.domain.goal.service.GoalService;
import com.walkit.walkit.domain.page.dto.ResponseHomeDto;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.global.exception.CustomException;
import com.walkit.walkit.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PageService {

    private final UserRepository userRepository;
    private final CharacterService characterService;
    private final GoalService goalService;

    public ResponseHomeDto getHomePage(Long userId, double lat, double lon) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ResponseCharacterDto characterDto = findCharacterDto(user, lat, lon);
        String walkProgressPercentage = goalService.findGoalProcess(user.getId()).getWalkProgressPercentage();

        return ResponseHomeDto.builder().characterDto(characterDto)
                .walkProgressPercentage(walkProgressPercentage)
                .build();
    }

    private ResponseCharacterDto findCharacterDto(User user, double lat, double lon) {
        return characterService.find(user.getId(), lat, lon);
    }
}
