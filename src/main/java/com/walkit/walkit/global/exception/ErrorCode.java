package com.walkit.walkit.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // USER
    USER_NOT_FOUND(NOT_FOUND, 1001, "[User] 사용자를 찾을 수 없습니다"),
    INVALID_TOKEN(UNAUTHORIZED, 1002, "[User] 토큰이 올바르지 않습니다"),
    USER_IMAGE_NOT_FOUND(NOT_FOUND, 1002, "[User] 사용자 이미지를 찾을 수 없습니다"),
    ALREADY_EXISTS_USER(BAD_REQUEST, 1003, "[User] 사용자가 이미 존재합니다."),
    INVALID_NICKNAME_FORMAT(BAD_REQUEST, 1004, "[User] 닉네임은 1~20자의 한글 또는 영문만 가능합니다."),
    INVALID_DATE_FORMAT(BAD_REQUEST, 1005, "[User] 날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)"),

    // FOLLOW
    FOLLOW_NOT_FOUND(NOT_FOUND, 2001, "[Follow] 팔로우을 찾을 수 없습니다"),
    ALREADY_EXISTS_PENDING_FOLLOW(NOT_ACCEPTABLE, 2002, "[Follow] 이미 요청중인 팔로우가 존재합니다."),
    ALREADY_EXISTS_ACCEPTED_FOLLOW(NOT_ACCEPTABLE, 2003, "[Follow] 이미 완료된 팔로우가 존재합니다."),
    CANT_FOLLOW_ONESELF(NOT_ACCEPTABLE, 2004, "[Follow] 자기 자신은 팔로우 할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}
