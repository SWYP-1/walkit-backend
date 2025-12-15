package com.walkit.walkit.domain.character.service;

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
public class CharacterService {

    private final UserRepository userRepository;

    public String getCharacterImageName(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        int characterLevel = user.getCharacter().getLevel();
        String asset = String.valueOf(user.getCharacter().getAsset());

        return "CHARACTER_" + "LEVEL_" + characterLevel + "ASSERT_" + asset + ".jpg";
    }
}
