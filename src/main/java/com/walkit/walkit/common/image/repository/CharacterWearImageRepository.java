package com.walkit.walkit.common.image.repository;

import com.walkit.walkit.common.image.entity.CharacterWearImage;
import com.walkit.walkit.domain.character.enums.Grade;
import com.walkit.walkit.domain.item.enums.ItemName;
import com.walkit.walkit.domain.item.enums.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CharacterWearImageRepository extends JpaRepository<CharacterWearImage, Long> {
    List<CharacterWearImage> findByPositionAndGrade(Position position, Grade grade);

    CharacterWearImage findByPositionAndGradeAndItemName(Position position, Grade grade, ItemName itemName);

    List<CharacterWearImage> findByGradeAndPosition(Grade grade, Position position);

    List<CharacterWearImage> findByItemName(ItemName itemName);

    List<CharacterWearImage> findByItemNameAndGradeAndPosition(ItemName itemName, Grade grade, Position position);
}
