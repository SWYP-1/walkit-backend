package com.walkit.walkit.domain.follow.entity;

import com.walkit.walkit.domain.follow.enums.FollowStatus;
import com.walkit.walkit.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Enumerated(EnumType.STRING)
    private FollowStatus followStatus = FollowStatus.PENDING;

    @Builder
    public Follow(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public void accept() {
        this.followStatus = FollowStatus.ACCEPTED;
    }
}
