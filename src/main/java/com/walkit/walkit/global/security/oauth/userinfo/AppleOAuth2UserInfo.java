package com.walkit.walkit.global.security.oauth.userinfo;

import java.util.Map;

public class AppleOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public AppleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        // Apple의 고유 사용자 ID (sub claim)
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        // Apple은 첫 로그인 시에만 이름을 제공
        // name이 없으면 이메일의 @ 앞부분을 사용
        String name = (String) attributes.get("name");
        if (name != null && !name.isEmpty()) {
            return name;
        }

        String email = getEmail();
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }

        return "Apple User";
    }

    @Override
    public String getProfileImageUrl() {
        // Apple은 프로필 이미지를 제공하지 않음
        return null;
    }
}
