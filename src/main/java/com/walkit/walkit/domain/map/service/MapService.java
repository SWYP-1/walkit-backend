package com.walkit.walkit.domain.map.service;

import com.walkit.walkit.domain.follow.entity.Follow;
import com.walkit.walkit.domain.follow.enums.FollowStatus;
import com.walkit.walkit.domain.follow.repository.FollowRepository;
import com.walkit.walkit.domain.map.dto.response.FollowerWalkingRecordResponseDto;
import com.walkit.walkit.domain.map.dto.response.MapCharacterDto;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.domain.walk.repository.WalkRepository;
import com.walkit.walkit.global.exception.CustomException;
import com.walkit.walkit.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
