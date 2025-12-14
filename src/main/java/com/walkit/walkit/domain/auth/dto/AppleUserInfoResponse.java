package com.walkit.walkit.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Apple ID Token에서 추출한 사용자 정보
 * Apple은 JWT 형식의 id_token을 사용하며, 표준 OAuth2 user info endpoint를 제공하지 않습니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppleUserInfoResponse {

    private String sub;      // Apple 고유 사용자 ID
    private String email;
    private String name;     // 첫 로그인 시에만 제공됨

    // 편의 메소드
    public String getId() {
        return sub;
    }

    public String getName() {
        // 이름이 없으면 이메일의 @ 앞부분을 이름으로 사용
        if (name != null && !name.isEmpty()) {
            return name;
        }

        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }

        return "Apple User";
    }

    public String getProfileImageUrl() {
        // Apple은 프로필 이미지를 제공하지 않음
        return null;
    }
}
