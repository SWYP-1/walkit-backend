package com.walkit.walkit.domain.walk.service;


import com.walkit.walkit.domain.walk.dto.request.WalkRequestDto;
import com.walkit.walkit.domain.walk.dto.response.FollowerWalkResponseDto;
import com.walkit.walkit.domain.walk.dto.response.WalkResponseDto;
import com.walkit.walkit.domain.walk.dto.response.WalkTotalSummaryResponseDto;

import java.util.List;


public interface WalkService {


    // 산책 종료 기록 저장
    WalkResponseDto saveWalk(Long userId, WalkRequestDto requestDto);

    // 산책 기록 조회(단건)
    WalkResponseDto getWalk(Long userId, Long walkId);


    // 산책 기록 수정
    void updateNote(Long userId, Long walkId, String note);


    // 산책 기록 총합 조회
    WalkTotalSummaryResponseDto getTotalSummary(Long userId);

    FollowerWalkResponseDto getWalkFollower(Long userId, String nickname, double lat, double lon);

    // 홈 페이지 - 당일 stepCount 조회
    int getTodayStepCount(Long userId);

    // 최근 7개 산책 기록 조회
    List<WalkResponseDto> getRecentWalks(Long userId);
}
