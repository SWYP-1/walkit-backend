package com.walkit.walkit.common.image.repository;

import com.walkit.walkit.common.image.entity.UserImage;
import com.walkit.walkit.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {
    Optional<UserImage> findByUserId(Long userId);
    void deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM UserImage ui WHERE ui.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
