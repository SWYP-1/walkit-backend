package com.walkit.walkit.domain.walks.controller;

import com.walkit.walkit.domain.walks.dto.request.WalkCompleteRequestDto;
import com.walkit.walkit.domain.walks.dto.request.WalkNoteUpdateRequestDto;
import com.walkit.walkit.domain.walks.dto.request.WalkStartRequestDto;
import com.walkit.walkit.domain.walks.dto.response.WalkDetailResponseDto;
import com.walkit.walkit.domain.walks.dto.response.WalkStartResponseDto;
import com.walkit.walkit.domain.walks.service.WalkService;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Tag(name = "Walk", description = "산책 기록 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/walk")
public class WalkController {

    private final WalkService walkService;


    @Operation(summary = "산책 시작", description = "산책 시작 시간과 시작 전 감정을 저장하고 walkId를 발급합니다.")
    @PostMapping("/start")
    public ResponseEntity<WalkStartResponseDto> startWalk(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody WalkStartRequestDto requestDto
    ) {
        Long userId = userPrincipal.getUserId();
        WalkStartResponseDto response = walkService.startWalk(userId, requestDto);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "산책 종료", description = "산책을 종료하고 전체 데이터를 저장합니다.")
    @PostMapping("/{walkId}/complete")
    public ResponseEntity<WalkDetailResponseDto> completeWalk(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long walkId,
            @RequestPart("data") WalkCompleteRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        request.setImage(image);
        Long userId = userPrincipal.getUserId();
        WalkDetailResponseDto response = walkService.completeWalk(userId, walkId, request);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "산책 기록 단건 조회", description = "산책 기록 ID로 내 산책 기록 1건을 조회합니다.")
    @GetMapping("/{walkId}")
    public ResponseEntity<WalkDetailResponseDto> getWalk(
            @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Long walkId) {

        Long userId = userPrincipal.getUserId();
        WalkDetailResponseDto response = walkService.getWalk(userId, walkId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "산책 기록 텍스트 수정", description = "산책 기록의 note(일기 텍스트)만 수정합니다.")
    @PatchMapping("/update/{walkId}")
    public ResponseEntity<Void> updateNote(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long walkId,
            @Valid @RequestBody WalkNoteUpdateRequestDto requestDto
    ) {
        Long userId = userPrincipal.getUserId();
        walkService.updateNote(userId, walkId, requestDto.getNote());
        return ResponseEntity.noContent().build();
    }



}

