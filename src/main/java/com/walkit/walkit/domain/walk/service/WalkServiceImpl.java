package com.walkit.walkit.domain.walk.service;

import com.walkit.walkit.common.image.enums.ImageType;
import com.walkit.walkit.common.image.service.ImageService;
import com.walkit.walkit.domain.character.dto.response.ResponseCharacterDto;
import com.walkit.walkit.domain.character.service.CharacterService;
import com.walkit.walkit.domain.follow.enums.FollowStatus;
import com.walkit.walkit.domain.follow.repository.FollowRepository;
import com.walkit.walkit.domain.goal.service.GoalService;
import com.walkit.walkit.domain.notification.service.GoalPushService;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.domain.walk.dto.request.WalkPointRequestDto;
import com.walkit.walkit.domain.walk.dto.request.WalkRequestDto;
import com.walkit.walkit.domain.walk.dto.response.FollowerWalkResponseDto;
import com.walkit.walkit.domain.walk.dto.response.WalkResponseDto;
import com.walkit.walkit.domain.walk.dto.response.WalkPointResponseDto;
import com.walkit.walkit.domain.walk.dto.response.WalkTotalSummaryResponseDto;
import com.walkit.walkit.domain.walk.entity.Walk;
import com.walkit.walkit.domain.walk.entity.WalkPoint;
import com.walkit.walkit.domain.walk.repository.WalkRepository;
import com.walkit.walkit.domain.walkLike.repository.WalkLikeRepository;
import com.walkit.walkit.global.exception.CustomException;
import com.walkit.walkit.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkServiceImpl implements WalkService {

    private final WalkRepository walkRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final GoalPushService goalPushService;

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private final GoalService goalService;
    private final FollowRepository followRepository;
    private final CharacterService characterService;
    private final WalkLikeRepository walkLikeRepository;


    // 산책 기록 저장
    @Override
    @Transactional
    public WalkResponseDto saveWalk(
            Long userId, WalkRequestDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Long start = request.getStartTime();
        Long end   = request.getEndTime();

        Long totalTime = null;
        if (start != null && end != null) {
            long diff = end - start;
            if (diff < 0) {
                throw new CustomException(ErrorCode.INVALID_WALK_TIME);
            }
            totalTime = diff;
        }

        // Walk 생성
        Walk walk = Walk.builder()
                .user(user)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalTime(totalTime)
                .preWalkEmotion(request.getPreWalkEmotion())
                .postWalkEmotion(request.getPostWalkEmotion())
                .stepCount(request.getStepCount())
                .totalDistance(request.getTotalDistance())
                .note(request.getNote())
                .build();

        walkRepository.save(walk);


        // points 저장
        if (request.getPoints() != null && !request.getPoints().isEmpty()) {
            List<WalkPoint> newPoints = new java.util.ArrayList<>();
            for (int i = 0; i < request.getPoints().size(); i++) {
                WalkPointRequestDto p = request.getPoints().get(i);
                validateLatLng(p.getLatitude(), p.getLongitude());
                if (p.getTimestampMillis() == null) throw new CustomException(ErrorCode.INVALID_TIMESTAMP);

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

        goalService.checkAchieveGoal(user, walk.getStepCount());

        // 목표(산책 횟수) 50%/100% 체크 후 알림 호출
        goalPushService.onWalkCompleted(user);


        return WalkResponseDto.fromDetail(walk);

    }


    // 산책 기록 조회(단건)
    @Override
    public WalkResponseDto getWalk(Long userId, Long walkId) {
        Walk walk = walkRepository.findDetailByIdAndUserId(walkId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WALK_NOT_FOUND));

        return WalkResponseDto.fromDetail(walk);

    }

    // 최근 산책 기록 7개 조회
    @Override
    public List<WalkResponseDto> getRecentWalks(Long userId) {
        return walkRepository
                .findTopByUserIdOrderByStartTimeDesc(userId, PageRequest.of(0, 7))
                .stream()
                .map(WalkResponseDto::fromDetail)
                .toList();
    }



    @Override
    public FollowerWalkResponseDto getWalkFollower(Long userId, String nickname, double lat, double lon) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User follower = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!(followRepository.existsBySenderAndReceiverAndFollowStatus(user, follower, FollowStatus.ACCEPTED)
        || followRepository.existsBySenderAndReceiverAndFollowStatus(follower, user, FollowStatus.ACCEPTED))
        ) {
            throw new CustomException(ErrorCode.FOLLOW_NOT_FOUND);
        }

        Walk walk = walkRepository.findFirstByUserIdOrderByCreatedDateDesc(follower.getId()).orElseThrow(() -> new CustomException(ErrorCode.WALK_NOT_FOUND));
      
        ResponseCharacterDto characterDto = characterService.find(follower.getId(), lat, lon);
        String walkProgressPercentage = goalService.findGoalProcess(follower.getId()).getWalkProgressPercentage();

        int likeCount = walkLikeRepository.findByWalk(walk).size();

        if (walkLikeRepository.existsByUserAndWalk(user, walk)) {
            return FollowerWalkResponseDto.from(characterDto, walkProgressPercentage, walk, true, likeCount);
        }

        return FollowerWalkResponseDto.from(characterDto, walkProgressPercentage, walk, false, likeCount);
    }



    // 산책 기록 수정
    @Override
    @Transactional
    public void updateNote(Long userId, Long walkId, String note) {
        Walk walk = walkRepository.findByIdAndUser_Id(walkId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WALK_NOT_FOUND));

        walk.updateNote(note);
    }


    // 전체 산책 기록 요약 조회(총 산책 횟수, 총 산책 시간)
    public WalkTotalSummaryResponseDto getTotalSummary(Long userId) {
        long count = walkRepository.countByUser_Id(userId);
        long totalTime = walkRepository.sumTotalTimeByUserId(userId);
        return new WalkTotalSummaryResponseDto(count, totalTime);
    }


    // 오늘 산책 걸음수 조회
    public int getTodayStepCount(Long userId) {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);

        long startMillis = today.atStartOfDay(zone).toInstant().toEpochMilli();
        long endMillis = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli();

        Integer sum = walkRepository.sumTodaySteps(userId, startMillis, endMillis);
        return sum == null ? 0 : sum;
    }

    // 사용자 전체 산책 기록 조회
    public List<WalkResponseDto> getAllWalks(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        return walkRepository.findAllDetailByUserId(userId)
                .stream()
                .map(WalkResponseDto::fromDetail)
                .toList();
    }


    private void validateLatLng(Double lat, Double lng) {
        if (lat == null || lng == null) throw new CustomException(ErrorCode.INVALID_LAT_LNG);
        if (lat < -90 || lat > 90) throw new CustomException(ErrorCode.INVALID_LAT_LNG);
        if (lng < -180 || lng > 180) throw new CustomException(ErrorCode.INVALID_LAT_LNG);
    }


}
