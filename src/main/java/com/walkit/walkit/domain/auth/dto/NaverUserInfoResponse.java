package com.walkit.walkit.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverUserInfoResponse {

    private String resultcode;
    private String message;
    private Response response;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private String id;
        private String email;
        private String name;

        @JsonProperty("profile_image")
        private String profileImage;
    }

    public String getId() {
        return response != null ? response.getId() : null;
    }

    public String getEmail() {
        return response != null ? response.getEmail() : null;
    }

    public String getName() {
        return response != null ? response.getName() : null;
    }

    public String getProfileImage() {
        return response != null ? response.getProfileImage() : null;
    }
}