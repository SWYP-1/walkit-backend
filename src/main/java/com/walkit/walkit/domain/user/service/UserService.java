package com.walkit.walkit.domain.user.service;

import com.walkit.walkit.common.image.enums.ImageType;
import com.walkit.walkit.common.image.repository.UserImageRepository;
import com.walkit.walkit.common.image.service.ImageService;
import com.walkit.walkit.common.image.service.UserImageService;
import com.walkit.walkit.domain.follow.entity.Follow;
import com.walkit.walkit.domain.follow.enums.FollowStatus;
import com.walkit.walkit.domain.follow.repository.FollowRepository;
import com.walkit.walkit.domain.user.dto.request.RequestPolicyDto;
import com.walkit.walkit.domain.user.dto.request.RequestUserDto;
import com.walkit.walkit.domain.user.dto.response.ResponseMarketingConsentDto;
import com.walkit.walkit.domain.user.dto.response.ResponseUserDto;
import com.walkit.walkit.domain.user.dto.response.ResponseUserNickNameFindDto;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.global.exception.CustomException;
import com.walkit.walkit.global.exception.ErrorCode;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserImageService userImageService;
    private final ImageService imageService;
    private final UserImageRepository userImageRepository;
    private final EntityManager entityManager;
    private final FollowRepository followRepository;

    public void savePolicy(Long userId, RequestPolicyDto dto) {
        User user = findUserById(userId);
        user.updatePolicy(dto);
    }

    public ResponseUserDto findUser(Long userId) {
        User user = findUserById(userId);
        String imageName = userImageService.findUserImageName(userId);

        return ResponseUserDto.from(imageName, user);
    }

    public void updateUser(Long userId, RequestUserDto dto, MultipartFile image) {
        User user = findUserById(userId);
        user.update(dto);

        // 이미지가 제공된 경우에만 업로드
        if (image != null && !image.isEmpty()) {

            if (userImageRepository.findByUserId(user.getId()).isPresent()) {
                String oldImageName = userImageService.delete(userId);
                entityManager.flush();
                imageService.deleteFile(oldImageName);
            }

            imageService.uploadFile(ImageType.USER, image, userId);
        }
    }

    public void saveNickname(Long userId, String nickname) {
        User user = findUserById(userId);
        user.updateNickname(nickname);
    }

    public void saveBirthDate(Long userId, LocalDate birthDate) {
        User user = findUserById(userId);
        user.updateBirthDate(birthDate);
    }

    public ResponseMarketingConsentDto checkMarketingConsent(Long userId) {
        User user = findUserById(userId);
        boolean marketingConsent = user.isMarketingConsent();

        return ResponseMarketingConsentDto.builder().isMarketingConsent(marketingConsent).build();
    }

    public void updateMarketingConsent(Long userId, boolean marketingConsent) {
        User user = findUserById(userId);
        user.updateMarketingConsent(marketingConsent);
    }

    public ResponseUserNickNameFindDto findUserByNickname(UserPrincipal userPrincipal, String nickname) {
        User targetUser = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String targetUserImageName = userImageService.findUserImageName(targetUser.getId());

        if (userPrincipal == null) {
            return ResponseUserNickNameFindDto.builder().userId(targetUser.getId()).nickName(nickname).imageName(targetUserImageName).build();
        } else {
            User user = findUserById(userPrincipal.getUserId());
            FollowStatus followStatus = null;
            if (followRepository.existsBySenderAndReceiver(user, targetUser)) {
                Follow follow = followRepository.findBySenderAndReceiver(user, targetUser);

                if (follow.getFollowStatus() == null) {
                    followStatus = FollowStatus.EMPTY;
                } else if (follow.getFollowStatus() == FollowStatus.PENDING) {
                    followStatus = FollowStatus.PENDING;
                } else if (follow.getFollowStatus() == FollowStatus.ACCEPTED) {
                    followStatus = FollowStatus.ACCEPTED;
                }
            } else if (followRepository.existsBySenderAndReceiver(targetUser, user)) {
                Follow follow = followRepository.findBySenderAndReceiver(targetUser, user);

                if (follow.getFollowStatus() == null) {
                    followStatus = FollowStatus.EMPTY;
                } else if (follow.getFollowStatus() == FollowStatus.PENDING) {
                    followStatus = FollowStatus.PENDING;
                } else if (follow.getFollowStatus() == FollowStatus.ACCEPTED) {
                    followStatus = FollowStatus.ACCEPTED;
                }
            }
            return ResponseUserNickNameFindDto.builder().userId(targetUser.getId()).nickName(nickname).imageName(targetUserImageName).followStatus(followStatus).build();
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
