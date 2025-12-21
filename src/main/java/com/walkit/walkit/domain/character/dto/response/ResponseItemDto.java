package com.walkit.walkit.domain.character.dto.response;

import com.walkit.walkit.domain.character.entity.Item;
import com.walkit.walkit.domain.character.enums.ItemName;
import com.walkit.walkit.domain.character.enums.Position;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseItemDto {

    private Long itemId;
    private ItemName name;
    private Position position;
    private boolean isOwned;
    private String imageName;

    public static ResponseItemDto from(Item item, boolean isOwned) {
        return ResponseItemDto.builder()
                .itemId(item.getId())
                .name(item.getItemName())
                .position(item.getPosition())
                .isOwned(isOwned)
                .imageName(item.getImageName())
                .build();
    }
}
