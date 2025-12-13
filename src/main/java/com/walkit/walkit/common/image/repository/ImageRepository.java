package com.walkit.walkit.common.image.repository;

import com.walkit.walkit.common.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image,Long> {
    void deleteByImageName(String imageName);
}
