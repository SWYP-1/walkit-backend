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

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserImageService {

    private final UserImageRepository userImageRepository;
    private final UserRepository userRepository;

    private final static String DEFAULT_USER_IMAGE_NAME = "USER_DEFAULT_IMAGE.png";

    public void saveUserImage(String imageName, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        UserImage userImage = UserImage.builder().user(user).imageName(imageName).build();
        userImageRepository.save(userImage);
    }

    public String findUserImageName(Long userId) {
        Optional<UserImage> userImage = userImageRepository.findByUserId(userId);

        if (userImage.isPresent()) {
            return userImage.get().getImageName();
        }

        return DEFAULT_USER_IMAGE_NAME;
    }

    public String delete(Long userId) {
        UserImage userImage = userImageRepository.findByUserId(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_IMAGE_NOT_FOUND));
        String imageName = userImage.getImageName();

        userImageRepository.delete(userImage);

        return imageName;
    }
}
