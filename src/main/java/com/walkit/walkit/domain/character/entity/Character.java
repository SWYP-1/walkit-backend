package com.walkit.walkit.domain.character.entity;

import com.walkit.walkit.common.image.entity.CharacterWearImage;
import com.walkit.walkit.domain.character.enums.Grade;
import com.walkit.walkit.domain.item.enums.Position;
import com.walkit.walkit.domain.item.entity.Item;
import com.walkit.walkit.domain.item.enums.Tag;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Table(name = "characters")
@NoArgsConstructor
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Grade grade = Grade.SEED;

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CharacterWear> characterWears;

    private String headImageName;

    @Enumerated(EnumType.STRING)
    private Tag headTag;

    private String bodyImageName;
    private String feetImageName;

    private int level = 0;

    @Builder
    public Character(Grade grade) {
        this.grade = grade;
    }

    public void updateImage(CharacterWearImage characterWearImage) {
        Position position = characterWearImage.getPosition();

        if (position == Position.HEAD) {
            this.headImageName = characterWearImage.getImageName();
        } else if (position == Position.BODY) {
            this.bodyImageName = characterWearImage.getImageName();
        } else if (position == Position.FEET) {
            this.feetImageName = characterWearImage.getImageName();
        }
    }

    public void updateImageToNull(Item item) {
        Position position = item.getPosition();

        if (position == Position.HEAD) {
            this.headImageName = null;
        } else if (position == Position.BODY) {
            this.bodyImageName = null;
        } else if (position == Position.FEET) {
            this.feetImageName = null;
        }
    }

    public void removeCharacterWear(CharacterWear characterWear) {
        this.characterWears.remove(characterWear);
    }

    public void updateLevel(int level) {
        this.level = level;
        gradeUp();
    }

    public boolean gradeUp() {
        if (this.level >= 1 && this.level <= 3) {
            this.grade = Grade.SEED;
            return true;
        } else if (this.level >= 4 && this.level <= 7) {
            this.grade = Grade.SPROUT;
            return true;
        } else if (this.level >= 8 && this.level <= 10) {
            this.grade = Grade.TREE;
            return true;
        }

        return false;
    }

}
