package com.walkit.walkit.domain.walkLike.service;

import com.walkit.walkit.domain.follow.enums.FollowStatus;
import com.walkit.walkit.domain.follow.repository.FollowRepository;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.domain.walk.entity.Walk;
import com.walkit.walkit.domain.walk.repository.WalkRepository;
import com.walkit.walkit.domain.walkLike.entity.WalkLike;
import com.walkit.walkit.domain.walkLike.repository.WalkLikeRepository;
import com.walkit.walkit.global.exception.CustomException;
import com.walkit.walkit.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class WalkLikeService {

    private final WalkLikeRepository walkLikeRepository;
    private final WalkRepository walkRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public void save(Long userId, Long walkId) {
        if (walkLikeRepository.existsByUserIdAndWalkId(userId, walkId)) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS_WALK_LIKE);
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Walk walk = walkRepository.findById(walkId).orElseThrow();
        User walker = walk.getUser();

        log.info("userId: {}", user.getId());
        log.info("walkerId: {}", walker.getId());


        if (!(followRepository.existsBySenderAndReceiverAndFollowStatus(user, walker, FollowStatus.ACCEPTED)
            || followRepository.existsBySenderAndReceiverAndFollowStatus(walker, user, FollowStatus.ACCEPTED)
        )) {
            throw new CustomException(ErrorCode.FOLLOW_NOT_FOUND);
        }

        // todo walk의 likeCount 필드에 1증가

        WalkLike walkLike = WalkLike.builder().user(user).walk(walk).build();
        walkLikeRepository.save(walkLike);
    }

    public void delete(Long userId, Long walkId) {
        Walk walk = walkRepository.findById(walkId).orElseThrow(() -> new CustomException(ErrorCode.WALK_NOT_FOUND));
        User follower = walk.getUser();

        if (!walkLikeRepository.existsByUserIdAndWalkId(userId, walkId)) {
            throw new CustomException(ErrorCode.WALK_LIKE_NOT_FOUND);
        }

        if (!(followRepository.existsBySenderIdAndReceiverIdAndFollowStatus(userId, follower.getId(), FollowStatus.ACCEPTED)
                || followRepository.existsBySenderIdAndReceiverIdAndFollowStatus(follower.getId(), userId, FollowStatus.ACCEPTED)
        )) {
            throw new CustomException(ErrorCode.FOLLOW_NOT_FOUND);
        }

        // todo walk의 likeCount 필드에 1 감소
        walkLikeRepository.deleteByUserIdAndWalkId(userId, walkId);
    }
}
