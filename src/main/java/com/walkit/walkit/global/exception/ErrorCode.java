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
    INVALID_TOKEN(UNAUTHORIZED, 1002, "[User] 토큰이 올바르지 않습니다");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}
