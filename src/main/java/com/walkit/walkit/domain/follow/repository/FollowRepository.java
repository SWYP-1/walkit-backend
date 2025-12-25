package com.walkit.walkit.domain.follow.repository;

import com.walkit.walkit.domain.follow.entity.Follow;
import com.walkit.walkit.domain.follow.enums.FollowStatus;
import com.walkit.walkit.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsBySenderAndReceiver(User user1, User user2);
    void deleteBySenderAndReceiver(User user1, User user2);

    List<Follow> findBySender(User sender);
    List<Follow> findByReceiver(User user);

    List<Follow> findByReceiverAndFollowStatus(User receiver, FollowStatus followStatus);

    Follow findBySenderAndReceiver(User user, User targetUser);
    Optional<Follow> findOptionalBySenderAndReceiver(User user, User targetUser);

    boolean existsBySenderAndReceiverAndFollowStatus(User sender, User receiver, FollowStatus followStatus);

    boolean existsBySenderIdAndReceiverIdAndFollowStatus(Long senderId, Long receiverId, FollowStatus followStatus);

    List<Follow> findBySenderAndFollowStatus(User sender, FollowStatus followStatus);
}
