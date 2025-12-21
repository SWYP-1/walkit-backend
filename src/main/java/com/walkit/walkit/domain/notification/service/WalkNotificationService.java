package com.walkit.walkit.domain.notification.service;

import com.walkit.walkit.domain.fcm.service.FcmMessagingService;
import com.walkit.walkit.domain.notification.entity.Notification;
import com.walkit.walkit.domain.notification.entity.NotificationType;
import com.walkit.walkit.domain.notification.repository.NotificationRepository;
import com.walkit.walkit.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalkNotificationService {

    private final FcmMessagingService fcmMessagingService;
    private final NotificationRepository notificationRepository;

     /**
     * 미접속 알림 (48시간 이상)
     */

     public boolean notifyInactiveUser(User user) {

         if (!user.canReceiveNotification()) {
             log.info("[NotifyInactive] notification disabled userId={}", user.getId());
             return false;
         }

         String title = "워키가 기다리고 있어요";
         String body = String.format(
                 "워키가 %s 님을 기다리고 있어요. 함께 산책하러 갈까요?",
                 user.getNickname()
         );

         Notification n = Notification.systemNotification(
                 user,
                 NotificationType.INACTIVE_USER,
                 title,
                 body,
                 null
         );
         notificationRepository.save(n);

         try {
             boolean ok = fcmMessagingService.sendNotification(
                     user,
                     title,
                     body,
                     Map.of("type", "INACTIVE_USER", "inactiveDays", "2")
             );
             log.info("[NotifyInactive48h] pushSent={} userId={}", ok, user.getId());
             return ok;
         } catch (Exception e) {
             log.warn("[NotifyInactive48h] push failed userId={}", user.getId(), e);
             return false;
         }
     }


    /**
     * 목표(산책 횟수) 50% 알림
     * - currentWalkCount / targetWalkCount 기준
     */
    public void notifyGoalHalfWalk(User user, int currentWalkCount, int targetWalkCount) {

        if (!user.canReceiveGoalNotification()) {
            log.info("[NotifyGoalHalf] goal notification disabled userId={}", user.getId());
            return;
        }

        Notification n = Notification.systemNotification(
                user,
                NotificationType.GOAL,
                "🏁 목표의 반을 달성했어요!",
                "목표의 반을 달성했어요! 지금 워키와 함께 걸어보세요",
                null
        );
        notificationRepository.save(n);

        fcmMessagingService.sendNotification(
                user,
                "🏁 목표의 반을 달성했어요!",
                "목표의 반을 달성했어요! 지금 워키와 함께 걸어보세요",
                Map.of(
                        "type", "GOAL_WALK_50",
                        "currentWalkCount", String.valueOf(currentWalkCount),
                        "targetWalkCount", String.valueOf(targetWalkCount)
                )
        );

    }

    /**
     * 목표(산책 횟수) 100% 달성 알림
     */
    public void notifyGoalFullWalk(User user, int currentWalkCount, int targetWalkCount) {

        if (!user.canReceiveGoalNotification()) {
            log.info("[NotifyGoalFull] goal notification disabled userId={}", user.getId());
            return;
        }

        Notification n = Notification.systemNotification(
                user,
                NotificationType.GOAL,
                "🎉 목표를 달성했어요!",
                "목표를 달성했어요! 워키의 성장을 함께 확인해볼까요?",
                null
        );
        notificationRepository.save(n);

        fcmMessagingService.sendNotification(
                user,
                "🎉 목표를 달성했어요!",
                "목표를 달성했어요! 워키의 성장을 함께 확인해볼까요?",
                Map.of(
                        "type", "GOAL_WALK_100",
                        "currentWalkCount", String.valueOf(currentWalkCount),
                        "targetWalkCount", String.valueOf(targetWalkCount)
                )
        );

    }


    /**
     * 새 미션 알림
     */

    public void notifyNewMission(User user) {

        // 미션 알림 OFF면 스킵
        if (!user.canReceiveMissionNotification()) {
            log.info("[NotifyMission] mission notification disabled userId={}", user.getId());
            return;
        }

        String title = "🎁 새로운 미션이 도착했어요!";
        String body = "미션을 완료하고 보상을 받아보세요!";

        Notification n = Notification.systemNotification(
                user,
                NotificationType.MISSION_OPEN,
                title,
                body,
                null
        );
        notificationRepository.save(n);

        try {
            boolean ok = fcmMessagingService.sendNotification(
                    user,
                    title,
                    body,
                    Map.of("type", "NEW_MISSION")
            );
            log.info("[NotifyMission] pushSent={} userId={}", ok, user.getId());
        } catch (Exception e) {
            log.warn("[NotifyMission] push failed userId={}", user.getId(), e);
        }
    }

    /**
     * 팔로우 요청 알림
     * sender -> receiver 에게 푸시 전송
     */
    public void notifyFollowRequest(User receiver, User sender) {

        if (!receiver.canReceiveFriendNotification()) {
            log.info("[NotifyFollowRequest] friend notification disabled receiverId={}", receiver.getId());
            return;
        }

        String title = "팔로우 요청";
        String body = String.format("%s님이 팔로우를 요청했어요.", sender.getNickname());

        fcmMessagingService.sendNotification(
                receiver,
                title,
                body,
                Map.of(
                        "type", "FOLLOW_REQUEST",
                        "senderId", String.valueOf(sender.getId()),
                        "senderNickname", sender.getNickname()
                )
        );
    }



}

