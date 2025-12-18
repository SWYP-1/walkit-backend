package com.walkit.walkit.common.image.service;

import com.walkit.walkit.common.image.entity.WalkImage;
import com.walkit.walkit.common.image.repository.WalkImageRepository;
import com.walkit.walkit.domain.walk.entity.Walk;
import com.walkit.walkit.domain.walk.repository.WalkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WalkImageService {

    private final WalkRepository walkRepository;
    private final WalkImageRepository walkImageRepository;

    public void saveWalkImage(String imageName, Long walkId) {
        Walk walk = walkRepository.findById(walkId)
                .orElseThrow(() -> new RuntimeException("Walk not found"));

        WalkImage walkImage = WalkImage.builder()
                .walk(walk)
                .imageName(imageName)
                .build();

        walkImageRepository.save(walkImage);
    }
}
