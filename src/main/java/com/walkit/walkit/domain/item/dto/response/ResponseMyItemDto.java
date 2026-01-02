package com.walkit.walkit.domain.item.dto.response;

import com.walkit.walkit.domain.item.entity.Item;
import com.walkit.walkit.domain.item.enums.ItemName;
import com.walkit.walkit.domain.item.enums.Position;
import com.walkit.walkit.domain.item.enums.Tag;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseMyItemDto {

    private Long itemId;
    private ItemName name;
    private Position position;
    private String imageName;
    private int point;
    private boolean isWorn;
    private Tag tag;

    public static ResponseMyItemDto from(Item item, boolean isWorn) {
        return ResponseMyItemDto.builder()
                .itemId(item.getId())
                .name(item.getItemName())
                .position(item.getPosition())
                .imageName(item.getImageName())
                .point(item.getPoint())
                .isWorn(isWorn)
                .build();
    }

    public static ResponseMyItemDto from(Item item, boolean isWorn, Tag tag) {
        return ResponseMyItemDto.builder()
                .itemId(item.getId())
                .name(item.getItemName())
                .position(item.getPosition())
                .imageName(item.getImageName())
                .point(item.getPoint())
                .isWorn(isWorn)
                .tag(tag)
                .build();
    }
}
