package com.walkit.walkit.common.image.entity;

import com.walkit.walkit.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class UserImage extends Image {

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public UserImage(Long id, String imageName, User user) {
        super(id, imageName);
        this.user = user;
    }
}
