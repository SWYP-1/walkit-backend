package com.walkit.walkit.domain.walk.service;


import com.walkit.walkit.domain.walk.dto.request.WalkRequestDto;
import com.walkit.walkit.domain.walk.dto.response.WalkResponseDto;


public interface WalkService {


    // 산책 종료 기록 저장
    WalkResponseDto saveWalk(Long userId, WalkRequestDto requestDto);

    // 산책 기록 조회(단건)
    WalkResponseDto getWalk(Long userId, Long walkId);

    // 산책 기록 수정
    void updateNote(Long userId, Long walkId, String note);





}
