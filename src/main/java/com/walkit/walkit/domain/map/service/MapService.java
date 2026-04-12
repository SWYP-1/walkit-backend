package com.walkit.walkit.domain.map.service;

import com.walkit.walkit.domain.follow.entity.Follow;
import com.walkit.walkit.domain.follow.enums.FollowStatus;
import com.walkit.walkit.domain.follow.repository.FollowRepository;
import com.walkit.walkit.domain.map.dto.response.FollowerRecentActivityResponseDto;
import com.walkit.walkit.domain.map.dto.response.FollowerWalkingRecordResponseDto;
import com.walkit.walkit.domain.map.dto.response.MapCharacterDto;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.domain.walk.entity.Walk;
import com.walkit.walkit.domain.walk.repository.WalkRepository;
import com.walkit.walkit.global.exception.CustomException;
import com.walkit.walkit.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapService {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final WalkRepository walkRepository;

    public List<FollowerWalkingRecordResponseDto> getFollowerWalkingRecords(Long userId, double lat, double lon, int radius) {
        User user = userRepository.findByIdAndDeleted(userId, false)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Follow> sentFollows = followRepository.findBySenderAndFollowStatus(user, FollowStatus.ACCEPTED);
        List<Follow> receivedFollows = followRepository.findByReceiverAndFollowStatus(user, FollowStatus.ACCEPTED);

        List<User> followUsers = Stream.concat(
                sentFollows.stream().map(Follow::getReceiver),
                receivedFollows.stream().map(Follow::getSender)
        ).distinct().toList();

        if (followUsers.isEmpty()) {
            return List.of();
        }

        List<Long> followUserIds = followUsers.stream().map(User::getId).toList();
        Map<Long, User> userMap = followUsers.stream().collect(Collectors.toMap(User::getId, u -> u));

        return walkRepository.findAllByUser_IdIn(followUserIds).stream()
                .filter(walk -> walk.getStartLatitude() != null && walk.getStartLongitude() != null)
                .filter(walk -> calculateHaversineDistance(lat, lon, walk.getStartLatitude(), walk.getStartLongitude()) <= radius)
                .map(walk -> {
                    User followUser = userMap.get(walk.getUser().getId());
                    return FollowerWalkingRecordResponseDto.builder()
                            .userId(followUser.getId())
                            .walkId(walk.getId())
                            .latitude(walk.getStartLatitude())
                            .longitude(walk.getStartLongitude())
                            .responseCharacterDto(MapCharacterDto.from(followUser.getCharacter()))
                            .build();
                })
                .toList();
    }

    public List<FollowerRecentActivityResponseDto> getFollowerRecentActivities(Long userId) {
        User user = userRepository.findByIdAndDeleted(userId, false)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Follow> sentFollows = followRepository.findBySenderAndFollowStatus(user, FollowStatus.ACCEPTED);
        List<Follow> receivedFollows = followRepository.findByReceiverAndFollowStatus(user, FollowStatus.ACCEPTED);

        List<User> followUsers = Stream.concat(
                sentFollows.stream().map(Follow::getReceiver),
                receivedFollows.stream().map(Follow::getSender)
        ).distinct().toList();

        if (followUsers.isEmpty()) {
            return List.of();
        }

        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        LocalDate yesterday = LocalDate.now(zoneId).minusDays(1);
        long yesterdayStart = yesterday.atStartOfDay(zoneId).toInstant().toEpochMilli();
        long yesterdayEnd = yesterday.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli();

        List<Long> followUserIds = followUsers.stream().map(User::getId).toList();
        Map<Long, User> userMap = followUsers.stream().collect(Collectors.toMap(User::getId, u -> u));

        // 유저별 산책 기록 그룹핑
        Map<Long, List<Walk>> walksByUser = walkRepository.findAllByUser_IdIn(followUserIds).stream()
                .collect(Collectors.groupingBy(walk -> walk.getUser().getId()));

        // 유저별 가장 최근 산책 시작 시간 (정렬 기준, 산책 기록 없으면 Long.MIN_VALUE)
        Map<Long, Long> latestStartTimeByUser = followUserIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> walksByUser.getOrDefault(id, List.of()).stream()
                                .mapToLong(Walk::getStartTime)
                                .max()
                                .orElse(Long.MIN_VALUE)
                ));

        return followUsers.stream()
                .sorted(Comparator.comparingLong((User u) -> latestStartTimeByUser.get(u.getId())).reversed())
                .map(followUser -> {
                    List<Walk> walks = walksByUser.getOrDefault(followUser.getId(), List.of());

                    boolean walkedYesterday = walks.stream()
                            .anyMatch(walk -> walk.getStartTime() != null
                                    && walk.getStartTime() >= yesterdayStart
                                    && walk.getStartTime() < yesterdayEnd);

                    return FollowerRecentActivityResponseDto.builder()
                            .userId(followUser.getId())
                            .nickName(followUser.getNickname())
                            .walkedYesterday(walkedYesterday)
                            .responseCharacterDto(MapCharacterDto.from(followUser.getCharacter()))
                            .build();
                })
                .toList();
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}
