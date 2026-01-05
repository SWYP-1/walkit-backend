package com.walkit.walkit.domain.follow.service;

import com.walkit.walkit.common.image.service.UserImageService;
import com.walkit.walkit.domain.follow.dto.response.ResponseFollowerDto;
import com.walkit.walkit.domain.follow.dto.response.ResponseFollowingDto;
import com.walkit.walkit.domain.follow.entity.Follow;
import com.walkit.walkit.domain.follow.enums.FollowStatus;
import com.walkit.walkit.domain.follow.repository.FollowRepository;
import com.walkit.walkit.domain.notification.entity.Notification;
import com.walkit.walkit.domain.notification.entity.NotificationType;
import com.walkit.walkit.domain.notification.repository.NotificationRepository;
import com.walkit.walkit.domain.notification.service.WalkNotificationService;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.walkit.walkit.global.exception.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final WalkNotificationService walkNotificationService;
    private final NotificationRepository notificationRepository;
    private final UserImageService userImageService;

    public void sendFollowing(Long userId, String nickname) {
        User sender = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        User receiver = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        checkOneSelfFollow(sender, receiver);
        checkAlreadyFollow(sender, receiver);

        Follow follow = Follow.builder().sender(sender).receiver(receiver).build();
        followRepository.save(follow);

        walkNotificationService.notifyFollowRequest(receiver, sender, follow.getId());
    }

    private void checkOneSelfFollow(User sender, User receiver) {
        if (sender.equals(receiver)) {
            throw new CustomException(CANT_FOLLOW_ONESELF);
        }
    }

    public void acceptFollow(Long userId, String nickname) {
        User followReceiver = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        User followSender = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        checkAlreadyAcceptedFollow(followReceiver, followSender);

        if (followRepository.existsBySenderAndReceiver(followReceiver, followSender)) {
            Follow follow = followRepository.findBySenderAndReceiver(followReceiver, followSender);
            follow.accept();
            deleteFollowNotification(follow.getId());
        }

        if (followRepository.existsBySenderAndReceiver(followSender, followReceiver)) {
            Follow follow = followRepository.findBySenderAndReceiver(followSender, followReceiver);
            follow.accept();
            deleteFollowNotification(follow.getId());
        }

        walkNotificationService.sendAcceptFollowNotificationToSender(followReceiver, followSender);
    }

    private void deleteFollowNotification(Long followId) {
        notificationRepository.deleteByTypeAndTargetId(NotificationType.FOLLOW, String.valueOf(followId));
    }

    public void deleteFollow(Long userId, String nickname) {
        User user1 = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        User user2 = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        checkNotAlreadyAcceptedFollow(user1, user2);

        if (followRepository.existsBySenderAndReceiver(user1, user2)) {
            followRepository.deleteBySenderAndReceiver(user1, user2);
        }

        if (followRepository.existsBySenderAndReceiver(user2, user1 )) {
            followRepository.deleteBySenderAndReceiver(user2, user1);
        }

    }

    public List<ResponseFollowerDto> findFollow(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        List<Follow> follows1 = followRepository.findBySenderAndFollowStatus(user, FollowStatus.ACCEPTED);
        List<Follow> follows2 = followRepository.findByReceiverAndFollowStatus(user, FollowStatus.ACCEPTED);

        List<User> user1 = follows1.stream().map(Follow::getReceiver).toList();
        List<User> user2 = follows2.stream().map(Follow::getSender).toList();

        List<User> allUser = Stream.concat(user1.stream(), user2.stream())
                .distinct()
                .toList();


        List<ResponseFollowerDto> followerDtos = new ArrayList<>();
        for (User findUser : allUser) {
            String imageName = userImageService.findUserImageName(findUser.getId());
            ResponseFollowerDto from = ResponseFollowerDto.from(findUser, imageName);
            followerDtos.add(from);
        }

        return followerDtos;
    }

    public List<ResponseFollowingDto> findRequestFollower(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        return followRepository.findByReceiverAndFollowStatus(user, FollowStatus.PENDING).stream()
                .map(ResponseFollowingDto::of)
                .toList();
    }

    public void deleteFollower(Long userId, String nickname) {
        User sender = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        User receiver = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (followRepository.existsBySenderAndReceiverAndFollowStatus(sender, receiver, FollowStatus.PENDING)) {
            Follow follow = followRepository.findBySenderAndReceiver(sender, receiver);

            notificationRepository.deleteByTypeAndTargetId(NotificationType.FOLLOW, String.valueOf(follow.getId()));
            followRepository.deleteBySenderAndReceiver(sender, receiver);

            deleteFollowNotification(follow.getId());
        } else {
            throw new CustomException(NOT_EXISTS_PENDING_FOLLOW);
        }
    }

    private void checkAlreadyFollow(User sender, User receiver) {
        checkAlreadyPendingFollow(sender, receiver);
        checkAlreadyAcceptedFollow(sender, receiver);
    }

    private void checkAlreadyAcceptedFollow(User sender, User receiver) {
        if (isAlreadyFollow(sender, receiver, FollowStatus.ACCEPTED)) {
            throw new CustomException(ALREADY_EXISTS_ACCEPTED_FOLLOW);
        }

        if (isAlreadyFollow(receiver, sender, FollowStatus.ACCEPTED)) {
            throw new CustomException(ALREADY_EXISTS_ACCEPTED_FOLLOW);
        }

    }

    private void checkAlreadyPendingFollow(User sender, User receiver) {
        if (isAlreadyFollow(sender, receiver, FollowStatus.PENDING)) {
            throw new CustomException(ALREADY_EXISTS_PENDING_FOLLOW);
        }
    }

    private boolean isAlreadyFollow(User user1, User user2, FollowStatus followStatus) {
        return followRepository.existsBySenderAndReceiverAndFollowStatus(user1, user2, followStatus);
    }

    private void checkNotAlreadyAcceptedFollow(User user1, User user2) {
        if (!isAlreadyFollow(user1, user2, FollowStatus.ACCEPTED) &&  !isAlreadyFollow(user2, user1, FollowStatus.ACCEPTED)) {
            throw new CustomException(FOLLOW_NOT_FOUND);
        }
    }
}
