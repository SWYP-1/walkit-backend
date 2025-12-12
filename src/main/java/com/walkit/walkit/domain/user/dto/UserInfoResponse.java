package com.walkit.walkit.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.global.common.enums.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserInfoResponse {

    @JsonProperty("user_id")
    private Long userId;

    private String email;
    private String name;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("auth_provider")
    private AuthProvider authProvider;

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .authProvider(user.getAuthProvider())
                .build();
    }
}
