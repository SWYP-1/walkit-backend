package com.walkit.walkit.common.image.entity;

import com.walkit.walkit.domain.character.enums.Grade;
import com.walkit.walkit.domain.item.enums.ItemName;
import com.walkit.walkit.domain.item.enums.Position;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class CharacterWearImage extends Image {

    @Enumerated(EnumType.STRING)
    private Position position;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Enumerated(EnumType.STRING)
    private ItemName itemName;

    @Builder
    public CharacterWearImage(Position position, Grade grade, ItemName itemName) {
        this.position = position;
        this.grade = grade;
        this.itemName = itemName;
    }
}
