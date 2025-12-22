package com.walkit.walkit.common.image.repository;

import com.walkit.walkit.common.image.entity.CharacterImage;
import com.walkit.walkit.domain.character.enums.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CharacterImageRepository extends JpaRepository<CharacterImage, Long> {
    CharacterImage findByGrade(Grade grade);
}
