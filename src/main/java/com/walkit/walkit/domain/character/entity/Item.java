package com.walkit.walkit.domain.character.entity;

import com.walkit.walkit.domain.character.enums.Position;
import com.walkit.walkit.domain.character.enums.ItemName;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(length = 255)
    private ItemName itemName;

    private int point;
    private String imageName;
    private int saleCount;

    @Builder
    public Item(Position position, ItemName itemName, int point) {
        this.position = position;
        this.itemName = itemName;
        this.point = point;
    }

    public void plusSaleCount() {
        this.saleCount += 1;
    }
}
