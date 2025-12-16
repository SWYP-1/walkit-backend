package com.walkit.walkit.domain.user.dto.response;

import com.walkit.walkit.domain.follow.enums.FollowStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseUserNickNameFindDto {

    private Long userId;
    private String imageName;
    private String nickName;
    private FollowStatus followStatus;
}
