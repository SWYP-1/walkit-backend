package com.walkit.walkit.domain.notification.service;

import com.walkit.walkit.domain.notification.dto.NotificationResponseDto;
import com.walkit.walkit.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponseDto> getNotifications(Long receiverId,int limit) {

        Pageable limitOnly = PageRequest.of(0, limit); // 0페이지 고정(최신 N개)
        return notificationRepository
                .findByReceiverIdOrderByCreatedDateDesc(receiverId, limitOnly)
                .stream()
                .map(n -> NotificationResponseDto.builder()
                        .notificationId(n.getId())
                        .type(n.getType().name())
                        .title(n.getTitle())
                        .body(n.getBody())
                        .senderId(n.getSender() != null ? n.getSender().getId() : null)
                        .senderNickname(n.getSender() != null ? n.getSender().getNickname() : null)
                        .targetId(n.getTargetId())
                        .isRead(n.isRead())
                        .createdAt(n.getCreatedDate())
                        .build()
                )
                .toList();
    }

    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        int deleted = notificationRepository.deleteByIdAndUserId(notificationId, userId);
        if (deleted == 0) {
            throw new IllegalArgumentException("Notification not found");
        }
    }


    public long getUnreadCount(Long receiverId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(receiverId);
    }
}
