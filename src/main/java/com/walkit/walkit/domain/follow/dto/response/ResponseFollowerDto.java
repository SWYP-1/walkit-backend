package com.walkit.walkit.domain.follow.dto.response;

import com.walkit.walkit.domain.follow.entity.Follow;
import com.walkit.walkit.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseFollowerDto {

    private String nickname;
    private Long userId;
    private String imageName;

    public static ResponseFollowerDto of(User user) {
        return ResponseFollowerDto.builder()
                .nickname(user.getNickname())
                .userId(user.getId())
                .imageName(user.getUserImage().getImageName())
                .build();
    }
}
