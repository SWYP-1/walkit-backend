package com.walkit.walkit.domain.character.dto.response;

import com.walkit.walkit.domain.item.enums.Position;
import com.walkit.walkit.domain.item.enums.Tag;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseCharacterWearDto {

    private String imageName;
    private Position itemPosition;
    private Tag itemTag;
}
