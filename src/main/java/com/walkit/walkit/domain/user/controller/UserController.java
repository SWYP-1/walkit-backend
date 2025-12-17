package com.walkit.walkit.domain.user.controller;

import com.walkit.walkit.domain.user.dto.request.RequestPolicyDto;
import com.walkit.walkit.domain.user.dto.request.RequestUserDto;
import com.walkit.walkit.domain.user.dto.response.ResponseMarketingConsentDto;
import com.walkit.walkit.domain.user.dto.response.ResponseUserDto;
import com.walkit.walkit.domain.user.dto.response.ResponseUserNickNameFindDto;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.domain.user.service.UserService;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/birth-date/{birthDate}")
    public ResponseEntity<Void> saveBirthDate(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String birthDate) {
        userService.saveBirthDate(userPrincipal.getUserId(), LocalDate.parse(birthDate));
        return ResponseEntity.status(CREATED).build();
    }

    @PostMapping("/policy")
    public ResponseEntity<Void> savePolicy(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody RequestPolicyDto dto) {
        userService.savePolicy(userPrincipal.getUserId(), dto);
        return ResponseEntity.status(CREATED).build();
    }

    @PostMapping("/nickname/{nickname}")
    public ResponseEntity<Void> saveNickname(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable @Pattern(regexp = "^[가-힣a-zA-Z]{1,20}$", message = "닉네임은 1~20자의 한글 또는 영문만 가능합니다.") String nickname) {
        userService.saveNickname(userPrincipal.getUserId(), nickname);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping
    public ResponseEntity<ResponseUserDto> findUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        ResponseUserDto dto = userService.findUser(userPrincipal.getUserId());
        return ResponseEntity.status(OK).body(dto);
    }

    @GetMapping("/nickname")
    public ResponseEntity<ResponseUserNickNameFindDto> findUserByNickname(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam String nickname) {
        ResponseUserNickNameFindDto dto = userService.findUserByNickname(userPrincipal, nickname);
        return  ResponseEntity.status(OK).body(dto);
    }

    @GetMapping("/marketing-consent")
    public ResponseEntity<ResponseMarketingConsentDto> isMarketingConsent(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        ResponseMarketingConsentDto dto = userService.checkMarketingConsent(userPrincipal.getUserId());
        return ResponseEntity.status(OK).body(dto);
    }

    @PatchMapping("/marketing-consent/{marketingConsent}")
    public ResponseEntity<Void> updateMarketingConsent(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable boolean marketingConsent) {
        userService.updateMarketingConsent(userPrincipal.getUserId(), marketingConsent);
        return ResponseEntity.status(OK).build();
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @ModelAttribute RequestUserDto dto) {

        userService.updateUser(userPrincipal.getUserId(), dto, dto.getImage());
        return ResponseEntity.status(OK).build();
    }
}
