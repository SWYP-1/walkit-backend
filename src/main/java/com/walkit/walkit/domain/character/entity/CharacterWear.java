package com.walkit.walkit.domain.character.entity;

import com.walkit.walkit.common.image.entity.CharacterWearImage;
import com.walkit.walkit.domain.item.entity.Item;
import com.walkit.walkit.domain.item.enums.Position;
import com.walkit.walkit.domain.item.enums.Tag;
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

    @Enumerated(EnumType.STRING)
    private Position position;

    @Enumerated(EnumType.STRING)
    private Tag tag;

    @Builder
    public CharacterWear(Character character, Item item, Position position, Tag tag) {
        this.character = character;
        this.item = item;
        this.position = position;
        this.tag = tag;
    }

    public static CharacterWear from(Character character, Item item, CharacterWearImage characterWearImage) {
        return CharacterWear.builder()
                .character(character)
                .item(item)
                .position(characterWearImage.getPosition())
                .tag(characterWearImage.getTag())
                .build();
    }

    public void updateTag(Tag tag) {
        this.tag = tag;
    }
}
