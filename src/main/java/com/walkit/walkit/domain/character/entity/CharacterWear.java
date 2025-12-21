package com.walkit.walkit.domain.character.entity;

import com.walkit.walkit.domain.character.enums.Position;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class CharacterWear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private Character character;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Builder
    public CharacterWear(Character character, Item item) {
        this.character = character;
        this.item = item;
    }

    public static CharacterWear from(Character character, Item item) {
        return CharacterWear.builder()
                .character(character)
                .item(item)
                .build();
    }
}
