package com.walkit.walkit.domain.follow.service;

import com.walkit.walkit.domain.follow.dto.response.ResponseFollowerDto;
import com.walkit.walkit.domain.follow.dto.response.ResponseFollowingDto;
import com.walkit.walkit.domain.follow.entity.Follow;
import com.walkit.walkit.domain.follow.enums.FollowStatus;
import com.walkit.walkit.domain.follow.repository.FollowRepository;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.global.exception.CustomException;
import com.walkit.walkit.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.walkit.walkit.global.exception.ErrorCode.FOLLOW_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public void sendFollow(Long userId, String nickname) {
        User sender = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User receiver = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Follow follow = Follow.builder().sender(sender).receiver(receiver).build();
        followRepository.save(follow);
    }

    public void acceptFollow(Long userId, Long followingId) {
        Follow follow = followRepository.findById(followingId).orElseThrow(() -> new CustomException(FOLLOW_NOT_FOUND));
        follow.accept();
    }

    public void deleteFollow(Long userId, String nickname) {
        User user1 = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User user2 = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (followRepository.existsBySenderAndReceiver(user1, user2)) {
            followRepository.deleteBySenderAndReceiver(user1, user2);
        }

        if (followRepository.existsBySenderAndReceiver(user2, user1 )) {
            followRepository.deleteBySenderAndReceiver(user2, user1);
        }

    }

    public List<ResponseFollowerDto> findFollowers(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Follow> follows1 = followRepository.findBySender(user);
        List<Follow> follows2 = followRepository.findByReceiver(user);

        List<User> user1 = follows1.stream().map(Follow::getReceiver).toList();
        List<User> user2 = follows2.stream().map(Follow::getSender).toList();

        user1.addAll(user2);

        List<User> allUsers = user1;

        return allUsers.stream().map(ResponseFollowerDto::of).toList();
    }

    public List<ResponseFollowingDto> findRequestFollowing(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return followRepository.findByReceiverAndFollowStatus(user, FollowStatus.PENDING).stream()
                .map(ResponseFollowingDto::of)
                .toList();
    }
}
