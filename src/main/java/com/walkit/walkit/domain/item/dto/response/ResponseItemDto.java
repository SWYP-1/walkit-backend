package com.walkit.walkit.domain.item.dto.response;

import com.walkit.walkit.domain.item.entity.Item;
import com.walkit.walkit.domain.item.enums.ItemName;
import com.walkit.walkit.domain.item.enums.Position;
import com.walkit.walkit.domain.item.enums.Tag;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseItemDto {

    private Long itemId;
    private ItemName name;
    private Position position;
    private boolean isOwned;
    private boolean isWorn;
    private String imageName;
    private int point;
    private Tag tag;

    public static ResponseItemDto from(Item item, boolean isOwned, boolean isWorn) {
        return ResponseItemDto.builder()
                .itemId(item.getId())
                .name(item.getItemName())
                .position(item.getPosition())
                .isOwned(isOwned)
                .isWorn(isWorn)
                .imageName(item.getImageName())
                .point(item.getPoint())
                .build();
    }

    public static ResponseItemDto from(Item item, boolean isOwned, boolean isWorn, Tag tag) {
        return ResponseItemDto.builder()
                .itemId(item.getId())
                .name(item.getItemName())
                .position(item.getPosition())
                .isOwned(isOwned)
                .isWorn(isWorn)
                .imageName(item.getImageName())
                .point(item.getPoint())
                .tag(tag)
                .build();
    }
}
