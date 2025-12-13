package com.walkit.walkit.domain.walks.service;

import com.walkit.walkit.common.image.enums.ImageType;
import com.walkit.walkit.common.image.service.ImageService;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.domain.walks.dto.request.WalkPointRequestDto;
import com.walkit.walkit.domain.walks.dto.request.WalkRequestDto;
import com.walkit.walkit.domain.walks.dto.response.WalkDetailResponseDto;
import com.walkit.walkit.domain.walks.dto.response.WalkPointResponseDto;
import com.walkit.walkit.domain.walks.dto.response.WalkResponseDto;
import com.walkit.walkit.domain.walks.entity.Walk;
import com.walkit.walkit.domain.walks.entity.WalkPoint;
import com.walkit.walkit.domain.walks.repository.WalkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkServiceImpl implements WalkService {

    private final WalkRepository walkRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;


    @Override
    @Transactional
    public WalkResponseDto saveWalk(Long userId, WalkRequestDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate walkedAt = (request.getWalkedAt() != null)
                ? request.getWalkedAt()
                : LocalDate.now();

        // 먼저 Walk 저장 (이미지 없이)
        Walk walk = Walk.create(
                user,
                request.getEmotion(),
                request.getText(),
                walkedAt,
                null
        );
        Walk saved = walkRepository.save(walk);

        // points 저장
        if (request.getPoints() != null && !request.getPoints().isEmpty()) {
            List<WalkPoint> newPoints = new java.util.ArrayList<>();
            for (int i = 0; i < request.getPoints().size(); i++) {
                WalkPointRequestDto p = request.getPoints().get(i);
                validateLatLng(p.getLatitude(), p.getLongitude());
                if (p.getTimestampMillis() == null) throw new IllegalArgumentException("timestampMillis required");

                Instant recordedAt = Instant.ofEpochMilli(p.getTimestampMillis());
                newPoints.add(WalkPoint.of(saved, i, p.getLatitude(), p.getLongitude(),recordedAt));
            }
            saved.replacePoints(newPoints);
            saved.updateStartEndFromPoints();      // start/end 요약 좌표 세팅
        }

        // 이미지 있으면 업로드
        MultipartFile image = request.getImage();
        if (image != null && !image.isEmpty()) {
            String imageUrl = imageService.uploadFile(ImageType.WALK, image, saved.getId());
            saved.updateImageUrl(imageUrl); // 대표 이미지 - 필요 없으면 주석 처리
        }

        return toSummaryResponse(saved);
    }


    @Override
    public WalkDetailResponseDto getWalk(Long userId, Long walkId) {
        Walk walk = walkRepository.findDetailByIdAndUserId(walkId, userId)
                .orElseThrow(() -> new RuntimeException("Walk not found"));

        return toDetailResponse(walk);
    }


    // 요약 응답
    private WalkResponseDto toSummaryResponse(Walk walk) {
        return WalkResponseDto.builder()
                .id(walk.getId())
                .emotion(walk.getEmotion())
                .text(walk.getText())
                .walkedAt(walk.getWalkedAt())
                .imageUrl(walk.getImageUrl())
                .createdAt(walk.getCreatedDate())
                .build();
    }

    // 상세 응답(points 추가)
    private WalkDetailResponseDto toDetailResponse(Walk walk) {
        List<WalkPointResponseDto> points = walk.getPoints().stream()
                .map(p -> new WalkPointResponseDto(
                       // p.getSeq(),
                        p.getLatitude(),
                        p.getLongitude(),
                        p.getRecordedAt() == null ? null : p.getRecordedAt().toEpochMilli()
                ))
                .toList();

        return WalkDetailResponseDto.builder()
                .id(walk.getId())
                .emotion(walk.getEmotion())
                .text(walk.getText())
                .walkedAt(walk.getWalkedAt())
                .imageUrl(walk.getImageUrl())
                .createdAt(walk.getCreatedDate())
                .points(points)
                .build();
    }

    private void validateLatLng(Double lat, Double lng) {
        if (lat == null || lng == null) throw new IllegalArgumentException("lat/lng required");
        if (lat < -90 || lat > 90) throw new IllegalArgumentException("invalid latitude");
        if (lng < -180 || lng > 180) throw new IllegalArgumentException("invalid longitude");
    }


}
