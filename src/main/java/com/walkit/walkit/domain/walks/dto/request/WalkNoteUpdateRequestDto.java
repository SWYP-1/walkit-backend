package com.walkit.walkit.domain.walks.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WalkNoteUpdateRequestDto {
    private String note;  // 수정할 텍스트
}