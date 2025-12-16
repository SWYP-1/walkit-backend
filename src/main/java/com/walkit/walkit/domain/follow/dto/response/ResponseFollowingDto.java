package com.walkit.walkit.domain.follow.dto.response;

import com.walkit.walkit.domain.follow.entity.Follow;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseFollowingDto {

    private String nickname;
    private Long userId;
    private Long followerId;

    public static ResponseFollowingDto of(Follow follow) {
        return ResponseFollowingDto.builder()
                .nickname(follow.getSender().getNickname())
                .userId(follow.getSender().getId())
                .followerId(follow.getId())
                .build();
    }
}
