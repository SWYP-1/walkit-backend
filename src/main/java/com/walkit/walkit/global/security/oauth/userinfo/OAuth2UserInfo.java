package com.walkit.walkit.global.security.oauth.userinfo;

public interface OAuth2UserInfo {
    String getProviderId();
    String getEmail();
    String getName();
    String getProfileImageUrl();
}
