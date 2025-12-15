package com.walkit.walkit.domain.walks.service;


import com.walkit.walkit.domain.walks.dto.request.WalkCompleteRequestDto;
import com.walkit.walkit.domain.walks.dto.request.WalkStartRequestDto;
import com.walkit.walkit.domain.walks.dto.response.WalkDetailResponseDto;
import com.walkit.walkit.domain.walks.dto.response.WalkStartResponseDto;
import org.springframework.web.multipart.MultipartFile;


public interface WalkService {

    // 산책 시작 기록 저장
    WalkStartResponseDto startWalk(Long userId, WalkStartRequestDto requestDto);

    // 산책 종료 기록 저장
    WalkDetailResponseDto completeWalk(Long userId, Long walkId, WalkCompleteRequestDto requestDto);

    // 산책 기록 조회(단건)
    WalkDetailResponseDto getWalk(Long userId, Long walkId);

    // 산책 기록 수정
    void updateNote(Long userId, Long walkId, String note);





}
