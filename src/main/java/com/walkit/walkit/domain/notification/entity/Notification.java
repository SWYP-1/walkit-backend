package com.walkit.walkit.domain.notification.entity;


import com.walkit.walkit.domain.BaseTimeEntity;
import com.walkit.walkit.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "notification")
public class Notification extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    // 누가 보냈는지(선택) - 시스템 알림이면 null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, length = 300)
    private String body;

    // 이동할 곳(선택): walkId/userId 등
    private String targetId;

    @Column(nullable = false)
    private boolean isRead;

    public void markRead() {
        this.isRead = true;
    }

    // 팔로우 알림 (발신자 존재)
    public static Notification userNotification(User receiver, User sender,
                                                NotificationType type,
                                                String title, String body,
                                                String targetId) {
        Notification n = new Notification();
        n.receiver = receiver;
        n.sender = sender;
        n.type = type;
        n.title = title;
        n.body = body;
        n.targetId = targetId;
        n.isRead = false;
        return n;
    }

    // 시스템 알림 (발신자 없음)
    public static Notification systemNotification(User receiver,
                                                  NotificationType type,
                                                  String title, String body,
                                                  String targetId) {
        Notification n = new Notification();
        n.receiver = receiver;
        n.sender = null;
        n.type = type;
        n.title = title;
        n.body = body;
        n.targetId = targetId;
        n.isRead = false;
        return n;
    }
}
