package com.walkit.walkit.domain.walks.controller;

import com.walkit.walkit.domain.walks.dto.request.WalkRequestDto;
import com.walkit.walkit.domain.walks.dto.response.WalkDetailResponseDto;
import com.walkit.walkit.domain.walks.dto.response.WalkResponseDto;
import com.walkit.walkit.domain.walks.service.WalkService;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Tag(name = "Walks", description = "산책 기록 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/walks")
public class WalkController {

    private final WalkService walkService;


    @Operation(summary = "산책 기록 저장", description = "산책 기록을 저장합니다.")
    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WalkResponseDto> createNotice(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                          @RequestPart("data") WalkRequestDto requestDto,
                                          @RequestPart(value = "image", required = false) MultipartFile image) {

        requestDto.setImage(image);
        Long userId = userPrincipal.getUserId();
        WalkResponseDto response = walkService.saveWalk(userId, requestDto);
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


}

