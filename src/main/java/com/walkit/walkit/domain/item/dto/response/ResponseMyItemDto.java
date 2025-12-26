package com.walkit.walkit.domain.item.dto.response;

import com.walkit.walkit.domain.item.entity.Item;
import com.walkit.walkit.domain.item.enums.ItemName;
import com.walkit.walkit.domain.item.enums.Position;
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

    public static ResponseMyItemDto of(Item item) {
        return ResponseMyItemDto.builder()
                .itemId(item.getId())
                .name(item.getItemName())
                .position(item.getPosition())
                .imageName(item.getImageName())
                .point(item.getPoint())
                .build();
    }

}
