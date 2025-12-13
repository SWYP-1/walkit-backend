package com.walkit.walkit.common.image.repository;

import com.walkit.walkit.common.image.entity.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {
    UserImage findByUserId(Long userId);
}
