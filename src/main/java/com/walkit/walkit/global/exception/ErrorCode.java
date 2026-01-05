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
    ALREADY_EXISTS_USER(CONFLICT, 1003, "[User] 사용자가 이미 존재합니다."),
    INVALID_NICKNAME_FORMAT(BAD_REQUEST, 1004, "[User] 닉네임은 1~20자의 한글 또는 영문만 가능합니다."),
    INVALID_DATE_FORMAT(BAD_REQUEST, 1005, "[User] 날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)"),
    INVALID_REQUEST(BAD_REQUEST,1006, "[User] 요청 값이 올바르지 않습니다."),
    USER_DELETED(GONE, 1007, "[User] 탈퇴한 회원입니다."),

    // FOLLOW
    FOLLOW_NOT_FOUND(NOT_FOUND, 2001, "[Follow] 팔로우를 찾을 수 없습니다"),
    ALREADY_EXISTS_PENDING_FOLLOW(CONFLICT, 2002, "[Follow] 이미 요청 중인 팔로우가 존재합니다."),
    ALREADY_EXISTS_ACCEPTED_FOLLOW(CONFLICT, 2003, "[Follow] 이미 완료된 팔로우가 존재합니다."),
    CANT_FOLLOW_ONESELF(BAD_REQUEST, 2004, "[Follow] 자기 자신은 팔로우 할 수 없습니다."),
    NOT_EXISTS_PENDING_FOLLOW(NOT_FOUND, 2005, "[Follow] 해당 유저에 요청중인 팔로우가 존재하지 않습니다"),

    // WALK_LIKE
    ALREADY_EXISTS_WALK_LIKE(CONFLICT, 3001, "[WalkLike] 해당 산책기록에 이미 좋아요를 눌렀습니다."),
    WALK_LIKE_NOT_FOUND(NOT_FOUND, 3002, "[WalkLike] 해당 산책기록에 좋아요를 누른 기록이 없습니다."),

    // ITEM
    ITEM_NOT_FOUND(NOT_FOUND, 4001, "[ITEM] 아이템을 찾을 수 없습니다."),
    ITEM_NOT_OWNED(FORBIDDEN, 4002, "[ITEM] 유저는 해당 아이템을 가지고 있지 않습니다."),
    INSUFFICIENT_FUNDS(BAD_REQUEST, 4004, "[ITEM] 아이템을 구매할 포인트가 부족합니다."),
    ALREADY_ITEM_OWNED(CONFLICT, 4005, "[ITEM] 해당 아이템을 이미 구매했습니다."),

    // WALK
    WALK_NOT_FOUND(NOT_FOUND, 5001, "[Walk] 산책 기록을 찾을 수 없습니다."),
    INVALID_WALK_TIME(BAD_REQUEST, 5002, "[Walk] 종료 시간은 시작 시간보다 빠를 수 없습니다."),
    INVALID_LAT_LNG(BAD_REQUEST, 5003, "[Walk] 위치 좌표가 올바르지 않습니다."),
    INVALID_TIMESTAMP(BAD_REQUEST, 5004, "[Walk] 위치 시간이 존재하지 않습니다."),

    // MISSION
    MISSION_NOT_FOUND(NOT_FOUND, 6001, "[Mission] 미션을 찾을 수 없습니다."),
    WEEKLY_MISSION_NOT_FOUND(NOT_FOUND, 6002, "[Mission] 이번 주 미션이 존재하지 않습니다."),
    MISSION_NOT_OWNED(FORBIDDEN, 6003, "[Mission] 본인의 미션이 아닙니다."),
    MISSION_ALREADY_COMPLETED(CONFLICT, 6004, "[Mission] 이미 완료한 미션입니다."),
    MISSION_NOT_ACHIEVED(BAD_REQUEST, 6005, "[Mission] 미션 목표를 아직 달성하지 못했습니다."),
    MISSION_TYPE_NOT_SUPPORTED(BAD_REQUEST, 6006, "[Mission] 지원하지 않는 미션 타입입니다."),
    MISSION_ASSIGN_FAILED(INTERNAL_SERVER_ERROR, 6007, "[Mission] 주간 미션 배정에 실패했습니다."),
    MISSION_CONFIG_INVALID(BAD_REQUEST, 6008, "[Mission] 미션 설정이 올바르지 않습니다."),

    // GOAL
    GOAL_UPDATE_NOT_ALLOWED(BAD_REQUEST, 7001, "[Goal] 목표 수정은 한달에 한번만 가능합니다."),

    // WEATHER
    WEATHER_PROVIDER_UNAVAILABLE(SERVICE_UNAVAILABLE, 8001,"[Weather] 외부 날씨 제공자가 일시적으로 사용 불가합니다."),
    WEATHER_CACHE_UNAVAILABLE(SERVICE_UNAVAILABLE, 8003, "[Weather] 캐시 갱신 중 문제가 발생했습니다.");


    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}