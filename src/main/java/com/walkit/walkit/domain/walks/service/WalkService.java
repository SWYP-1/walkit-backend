package com.walkit.walkit.domain.walks.service;


import com.walkit.walkit.domain.walks.dto.request.WalkRequestDto;
import com.walkit.walkit.domain.walks.dto.response.WalkDetailResponseDto;
import com.walkit.walkit.domain.walks.dto.response.WalkResponseDto;


public interface WalkService {

    // 산책 기록 저장
    WalkResponseDto saveWalk(Long userId, WalkRequestDto request);

    // 산책 기록 조회(단건)
    WalkDetailResponseDto getWalk(Long userId, Long walkId);




}
