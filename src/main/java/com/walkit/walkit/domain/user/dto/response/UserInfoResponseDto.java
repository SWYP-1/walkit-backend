package com.walkit.walkit.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.common.enums.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserInfoResponseDto {

    @JsonProperty("user_id")
    private Long userId;

    private String email;
    private String name;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("auth_provider")
    private AuthProvider authProvider;

    public static UserInfoResponseDto from(User user) {
        return UserInfoResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .authProvider(user.getAuthProvider())
                .build();
    }
}
