package com.walkit.walkit.domain.user.service;

import com.walkit.walkit.common.image.enums.ImageType;
import com.walkit.walkit.common.image.repository.UserImageRepository;
import com.walkit.walkit.common.image.service.ImageService;
import com.walkit.walkit.common.image.service.UserImageService;
import com.walkit.walkit.domain.character.dto.response.ResponseCharacterDto;
import com.walkit.walkit.domain.character.service.CharacterService;
import com.walkit.walkit.domain.follow.entity.Follow;
import com.walkit.walkit.domain.follow.enums.FollowStatus;
import com.walkit.walkit.domain.follow.repository.FollowRepository;
import com.walkit.walkit.domain.user.dto.request.RequestPolicyDto;
import com.walkit.walkit.domain.user.dto.request.RequestUserDto;
import com.walkit.walkit.domain.user.dto.response.*;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.domain.walk.dto.response.WalkTotalSummaryResponseDto;
import com.walkit.walkit.domain.walk.service.WalkService;
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
import java.time.LocalDateTime;

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
    private final CharacterService characterService;
    private final WalkService walkService;

    public void savePolicy(Long userId, RequestPolicyDto dto) {
        User user = findUserById(userId);
        user.updatePolicy(dto);
    }

    public ResponseUserDto findUser(Long userId) {
        User user = findUserById(userId);
        String imageName = userImageService.findUserImageName(userId);

        return ResponseUserDto.from(imageName, user);
    }

    public void updateUser(Long userId, RequestUserDto dto) {
        User user = findUserById(userId);

        checkExistsUserByNickname(user, dto.getNickname());

        user.update(dto);
    }

    public void updateUserImage(Long userId, MultipartFile image) {
        User user = findUserById(userId);

        // 이미지가 제공된 경우에만 업로드
        if (image != null && !image.isEmpty()) {

            if (userImageRepository.findByUserId(user.getId()).isPresent()) {
                log.info("user image already exists");
                String oldImageName = userImageService.delete(userId);
                log.info("oldImageName : " + oldImageName);
                imageService.deleteFile(oldImageName);
                entityManager.flush();
            }

            imageService.uploadFile(ImageType.USER, image, userId);
        }
    }

    public void saveNickname(Long userId, String nickname) {
        checkExistsUserByNickname(nickname);

        User user = findUserById(userId);
        user.updateNickname(nickname);
    }

    private void checkExistsUserByNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS_USER);
        }
    }

    private void checkExistsUserByNickname(User user, String nickname) {
        if (userRepository.existsByNickname(nickname) && (user.getNickname() != null && !user.getNickname().equals(nickname))) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS_USER);
        }

        if (userRepository.existsByNickname(nickname) && user.getNickname() == null) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS_USER);
        }
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

            if (user == targetUser) {
                return ResponseUserNickNameFindDto.builder().userId(targetUser.getId()).nickName(nickname).imageName(targetUserImageName).followStatus(FollowStatus.MYSELF).build();

            }


            FollowStatus followStatus = null;
            if (followRepository.existsBySenderAndReceiver(user, targetUser)) {
                Follow follow = followRepository.findBySenderAndReceiver(user, targetUser);

                if (follow == null) {
                    followStatus = FollowStatus.EMPTY;
                } else if (follow.getFollowStatus() == FollowStatus.PENDING) {
                    followStatus = FollowStatus.PENDING;
                } else if (follow.getFollowStatus() == FollowStatus.ACCEPTED) {
                    followStatus = FollowStatus.ACCEPTED;
                }
            } else if (followRepository.existsBySenderAndReceiver(targetUser, user)) {
                Follow follow = followRepository.findBySenderAndReceiver(targetUser, user);

                if (follow == null) {
                    followStatus = FollowStatus.EMPTY;
                } else if (follow.getFollowStatus() == FollowStatus.PENDING) {
                    followStatus = FollowStatus.PENDING;
                } else if (follow.getFollowStatus() == FollowStatus.ACCEPTED) {
                    followStatus = FollowStatus.ACCEPTED;
                }
            } else {
                followStatus = FollowStatus.EMPTY;
            }
            return ResponseUserNickNameFindDto.builder().userId(targetUser.getId()).nickName(nickname).imageName(targetUserImageName).followStatus(followStatus).build();
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }


    @Transactional
    public void updateLastAccessAt(Long userId, LocalDateTime now) {
        userRepository.updateLastAccessAt(userId, now);
    }

    public LocalDateTime findLastAccessAt(Long userId) {
        return userRepository.findLastAccessAt(userId);
    }

    public ResponsePointDto findPoint(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponsePointDto.builder().point(0).build();
        }

        User user = findUserById(userPrincipal.getUserId());

        return ResponsePointDto.builder().point(user.getPoint()).build();
    }


    public ResponseUserSummaryDto findUserSummary(String nickname, double lat, double lon) {
        User user = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        ResponseCharacterDto responseCharacterDto = characterService.find(user.getId(), lat, lon);
        WalkTotalSummaryResponseDto totalSummary = walkService.getTotalSummary(user.getId());

        return ResponseUserSummaryDto.builder().responseCharacterDto(responseCharacterDto).walkTotalSummaryResponseDto(totalSummary).build();
    }
}
