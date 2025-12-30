
package com.walkit.walkit.domain.notification.repository;


import com.walkit.walkit.domain.notification.entity.Notification;
import com.walkit.walkit.domain.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverIdOrderByCreatedDateDesc(Long receiverId, Pageable pageable);
    long countByReceiverIdAndIsReadFalse(Long receiverId);

    void deleteByTypeAndTargetId(NotificationType type, String targetId);
}
