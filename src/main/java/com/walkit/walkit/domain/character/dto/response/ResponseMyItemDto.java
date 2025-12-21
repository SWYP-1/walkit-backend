package com.walkit.walkit.domain.character.dto.response;

import com.walkit.walkit.domain.character.entity.Item;
import com.walkit.walkit.domain.character.enums.ItemName;
import com.walkit.walkit.domain.character.enums.Position;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseMyItemDto {

    private Long itemId;
    private ItemName name;
    private Position position;
    private String imageName;

    public static ResponseMyItemDto of(Item item) {
        return ResponseMyItemDto.builder()
                .itemId(item.getId())
                .name(item.getItemName())
                .position(item.getPosition())
                .imageName(item.getImageName())
                .build();
    }

}
