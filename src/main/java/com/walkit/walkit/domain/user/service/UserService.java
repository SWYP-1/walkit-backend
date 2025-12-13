package com.walkit.walkit.domain.user.service;

import com.walkit.walkit.domain.user.dto.request.RequestPolicyDto;
import com.walkit.walkit.domain.user.dto.request.RequestUserDto;
import com.walkit.walkit.domain.user.dto.response.ResponseSubscribeDto;
import com.walkit.walkit.domain.user.dto.response.ResponseUserDto;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.RefreshTokenRepository;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.global.exception.CustomException;
import com.walkit.walkit.global.exception.ErrorCode;
import com.walkit.walkit.global.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;


    public void saveBirthYear(Long userId, int year) {
        User user = findUserById(userId);
        user.updateBirthYear(year);
    }

    public void savePolicy(Long userId, RequestPolicyDto dto) {
        User user = findUserById(userId);
        user.updatePolicy(dto);
    }

    public ResponseUserDto findUser(Long userId) {
        User user = findUserById(userId);
        return ResponseUserDto.from("aa", user);
    }

    public ResponseSubscribeDto isSubscribed(Long userId) {
        User user = findUserById(userId);
        return ResponseSubscribeDto.builder().isSubscribed(user.isSubscribed()).build();
    }

    public void updateSubscribed(Long userId, boolean isSubscribed) {
        User user = findUserById(userId);
        user.updateIsSubscribed(isSubscribed);
    }

    public void changeUser(Long userId, RequestUserDto dto, MultipartFile image) {
        User user = findUserById(userId);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
