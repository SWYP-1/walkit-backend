package com.walkit.walkit.domain.character.entity;

import com.walkit.walkit.domain.character.enums.Grade;
import com.walkit.walkit.domain.character.enums.Position;
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
    private String bodyImageName;
    private String feetImageName;

    private int level = 1;

    @Builder
    public Character(Grade grade) {
        this.grade = grade;
    }

    public void updateImage(Item item) {
        Position position = item.getPosition();

        if (position == Position.HEAD) {
            this.headImageName = item.getImageName();
        } else if (position == Position.BODY) {
            this.bodyImageName = item.getImageName();
        } else if (position == Position.FEET) {
            this.feetImageName = item.getImageName();
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
}
