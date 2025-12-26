package com.walkit.walkit.domain.item.entity;

import com.walkit.walkit.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ItemManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private boolean isActive = false;

    @Builder
    public ItemManagement(User user, Item item, boolean isActive) {
        this.user = user;
        this.item = item;
        this.isActive = isActive;
    }

    public static ItemManagement from(User user, Item item) {
        return new ItemManagement(user, item, false);
    }

    public void active() {
        this.isActive = true;
    }

    public void inActive() {
        this.isActive = false;
    }
}
