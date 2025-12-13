package com.walkit.walkit.domain.user.controller;

import com.walkit.walkit.domain.user.dto.request.RequestPolicyDto;
import com.walkit.walkit.domain.user.dto.request.RequestUserDto;
import com.walkit.walkit.domain.user.dto.response.ResponseSubscribeDto;
import com.walkit.walkit.domain.user.dto.response.ResponseUserDto;
import com.walkit.walkit.domain.user.dto.response.UserInfoResponseDto;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.domain.user.service.UserService;
import com.walkit.walkit.global.exception.CustomException;
import com.walkit.walkit.global.exception.ErrorCode;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/birth-year")
    public ResponseEntity<Void> saveBirthYear(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam int year) {
        userService.saveBirthYear(userPrincipal.getUserId(), year);
        return ResponseEntity.status(CREATED).build();
    }

    @PostMapping("/policy")
    public ResponseEntity<Void> savePolicy(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody RequestPolicyDto dto) {
        userService.savePolicy(userPrincipal.getUserId(), dto);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping
    public ResponseEntity<ResponseUserDto> findUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        ResponseUserDto dto = userService.findUser(userPrincipal.getUserId());
        return ResponseEntity.status(OK).body(dto);
    }

    @GetMapping("/is-subscribed")
    public ResponseEntity<ResponseSubscribeDto> isSubscribed(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        ResponseSubscribeDto dto = userService.isSubscribed(userPrincipal.getUserId());
        return ResponseEntity.status(OK).body(dto);
    }

    @PutMapping("/is-subscribed")
    public ResponseEntity<Void> updateSubscribed(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam boolean isSubscribed) {
        userService.updateSubscribed(userPrincipal.getUserId(), isSubscribed);
        return ResponseEntity.status(OK).build();
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> changeUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart RequestUserDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image
            ) {
        userService.changeUser(userPrincipal.getUserId(), dto, image);
        return ResponseEntity.status(OK).build();
    }
}
