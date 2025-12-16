package com.walkit.walkit.common.image.repository;

import com.walkit.walkit.common.image.entity.UserImage;
import com.walkit.walkit.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {
    Optional<UserImage> findByUserId(Long userId);
    void deleteByUser(User user);
    void deleteByUserId(Long userId);
}
