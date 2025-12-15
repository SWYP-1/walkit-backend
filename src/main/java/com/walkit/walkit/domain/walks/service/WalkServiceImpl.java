package com.walkit.walkit.domain.walks.service;

import com.walkit.walkit.common.image.enums.ImageType;
import com.walkit.walkit.common.image.service.ImageService;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.domain.walks.dto.request.WalkPointRequestDto;
import com.walkit.walkit.domain.walks.dto.request.WalkRequestDto;
import com.walkit.walkit.domain.walks.dto.response.WalkResponseDto;
import com.walkit.walkit.domain.walks.dto.response.WalkPointResponseDto;
import com.walkit.walkit.domain.walks.entity.Walk;
import com.walkit.walkit.domain.walks.entity.WalkPoint;
import com.walkit.walkit.domain.walks.repository.WalkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkServiceImpl implements WalkService {

    private final WalkRepository walkRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;




    // 산책 저장
    @Override
    @Transactional
    public WalkResponseDto saveWalk(
            Long userId, WalkRequestDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Walk 생성
        Walk walk = Walk.builder()
                .user(user)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .preWalkEmotion(request.getPreWalkEmotion())
                .postWalkEmotion(request.getPostWalkEmotion())
                .stepCount(request.getStepCount())
                .totalDistance(request.getTotalDistance())
                .note(request.getNote())
                .build();

        Walk saved = walkRepository.save(walk);

        // points 저장
        if (request.getPoints() != null && !request.getPoints().isEmpty()) {
            List<WalkPoint> newPoints = new java.util.ArrayList<>();
            for (int i = 0; i < request.getPoints().size(); i++) {
                WalkPointRequestDto p = request.getPoints().get(i);
                validateLatLng(p.getLatitude(), p.getLongitude());
                if (p.getTimestampMillis() == null) throw new IllegalArgumentException("timestampMillis required");

                Long recordedAt = p.getTimestampMillis();
                newPoints.add(WalkPoint.of(walk, p.getLatitude(), p.getLongitude(), recordedAt));
            }
            walk.replacePoints(newPoints);
            walk.updateStartEndFromPoints();      // start/end 요약 좌표 세팅
        }


        // 이미지 업로드
        MultipartFile image = request.getImage();
        if (image != null && !image.isEmpty()) {
            String imageUrl = imageService.uploadFile(ImageType.WALK, image, walk.getId());
            walk.updateImageUrl(imageUrl); // 대표 이미지 - 필요 없으면 주석 처리
        }

        return toDetailResponse(walk);
    }


    // 산책 기록 조회 (단건)
    @Override
    public WalkResponseDto getWalk(Long userId, Long walkId) {
        Walk walk = walkRepository.findDetailByIdAndUserId(walkId, userId)
                .orElseThrow(() -> new RuntimeException("Walk not found"));

        return toDetailResponse(walk);
    }


    // 산책 기록 수정
    @Override
    @Transactional
    public void updateNote(Long userId, Long walkId, String note) {
        Walk walk = walkRepository.findByIdAndUser_Id(walkId, userId)
                .orElseThrow(() -> new IllegalArgumentException("산책 기록을 찾을 수 없습니다."));

        walk.updateNote(note);
    }

    // 상세 응답
    private WalkResponseDto toDetailResponse(Walk walk) {
        List<WalkPointResponseDto> points = walk.getPoints().stream()
                .map(p -> new WalkPointResponseDto(
                        p.getLatitude(),
                        p.getLongitude(),
                        p.getRecordedAt() == null ? null : p.getRecordedAt()
                ))
                .toList();

        return WalkResponseDto.builder()
                .id(walk.getId())
                .preWalkEmotion(walk.getPreWalkEmotion())
                .postWalkEmotion(walk.getPostWalkEmotion())
                .note(walk.getNote())
                .stepCount(walk.getStepCount())
                .totalDistance(walk.getTotalDistance())
                .startTime(walk.getStartTime())
                .endTime(walk.getEndTime())
                .imageUrl(walk.getImageUrl())
                .createdDate(walk.getCreatedDate())
                .points(points)
                .build();
    }

    private void validateLatLng(Double lat, Double lng) {
        if (lat == null || lng == null) throw new IllegalArgumentException("lat/lng required");
        if (lat < -90 || lat > 90) throw new IllegalArgumentException("invalid latitude");
        if (lng < -180 || lng > 180) throw new IllegalArgumentException("invalid longitude");
    }


}
