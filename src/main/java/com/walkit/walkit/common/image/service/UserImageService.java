package com.walkit.walkit.common.image.service;

import com.walkit.walkit.common.image.entity.UserImage;
import com.walkit.walkit.common.image.repository.UserImageRepository;
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
public class UserImageService {

    private final UserImageRepository userImageRepository;
    private final UserRepository userRepository;

    public void saveUserImage(String imageName, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        UserImage userImage = UserImage.builder().user(user).imageName(imageName).build();
        userImageRepository.save(userImage);
    }
}
