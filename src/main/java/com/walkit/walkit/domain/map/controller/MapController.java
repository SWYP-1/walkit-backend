package com.walkit.walkit.domain.map.controller;

import com.walkit.walkit.domain.map.dto.response.FollowerWalkingRecordResponseDto;
import com.walkit.walkit.domain.map.service.MapService;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Map", description = "지도 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/maps")
public class MapController {

    private final MapService mapService;

    @Operation(summary = "팔로우 산책 기록 위치 조회", description = "지도 탭에서 나의 팔로우들의 최근 산책 기록 위치를 반경 내에서 조회합니다.")
    @GetMapping("/follower/walking-records")
    public ResponseEntity<List<FollowerWalkingRecordResponseDto>> getFollowerWalkingRecords(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "1000") int radius
    ) {
        Long userId = userPrincipal.getUserId();
        List<FollowerWalkingRecordResponseDto> response = mapService.getFollowerWalkingRecords(userId, lat, lon, radius);
        return ResponseEntity.ok(response);
    }
}
