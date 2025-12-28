package com.walkit.walkit.domain.user.dto.response;

import com.walkit.walkit.domain.character.dto.response.ResponseCharacterDto;
import com.walkit.walkit.domain.walk.dto.response.WalkTotalSummaryResponseDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseUserSummaryDto {

    private ResponseCharacterDto responseCharacterDto;
    private WalkTotalSummaryResponseDto walkTotalSummaryResponseDto;
}
