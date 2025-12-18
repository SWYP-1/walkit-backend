package com.walkit.walkit.domain.walk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walkit.walkit.domain.walk.dto.request.WalkRequestDto;
import com.walkit.walkit.domain.walk.dto.request.WalkNoteUpdateRequestDto;
import com.walkit.walkit.domain.walk.dto.response.WalkResponseDto;
import com.walkit.walkit.domain.walk.service.WalkService;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
    private final ObjectMapper objectMapper = new ObjectMapper();


    @PostMapping(value="/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WalkResponseDto> saveWalk(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("data") String data,
            @RequestPart(value="image", required=false) MultipartFile image
    ) throws Exception {
        WalkRequestDto dto = objectMapper.readValue(data, WalkRequestDto.class);
        dto.setImage(image);
        return ResponseEntity.ok(walkService.saveWalk(principal.getUserId(), dto));
    }




    @Operation(summary = "산책 기록 단건 조회", description = "산책 기록 ID로 내 산책 기록 1건을 조회합니다.")
    @GetMapping("/{walkId}")
    public ResponseEntity<WalkResponseDto> getWalk(
            @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Long walkId) {

        Long userId = userPrincipal.getUserId();
        WalkResponseDto response = walkService.getWalk(userId, walkId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "산책 기록 날짜 조회", description = "startTime(epoch millis)이 속한 날짜(KST) 기준으로 내 산책 기록 1건을 조회합니다.")
    @GetMapping()
    public ResponseEntity<WalkResponseDto> getDailyWalk(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam long startTime
    ) {
        Long userId = principal.getUserId();
        WalkResponseDto dto = walkService.getWalkByDay(userId, startTime);
        return ResponseEntity.ok(dto);
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

