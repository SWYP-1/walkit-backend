package com.walkit.walkit.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseUserNickNameFindDto {

    private Long userId;
    private String imageName;
    private String nickName;
}
