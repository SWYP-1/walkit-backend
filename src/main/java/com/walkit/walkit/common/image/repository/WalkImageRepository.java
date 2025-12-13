package com.walkit.walkit.common.image.repository;

import com.walkit.walkit.common.image.entity.WalkImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalkImageRepository extends JpaRepository<WalkImage, Long> {
    List<WalkImage> findAllByWalkId(Long walkId);
    void deleteAllByWalkId(Long walkId);
}
