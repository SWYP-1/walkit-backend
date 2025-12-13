package com.walkit.walkit.global.security.oauth;

import com.walkit.walkit.domain.user.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    private final Long userId;
    private final String email;
    private final UserRole role;

    public CustomOAuth2User(Long userId,
                            String email,
                            UserRole role,
                            Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes,
                            String nameAttributeKey) {
        super(authorities, attributes, nameAttributeKey);
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public static CustomOAuth2User create(Long userId,
                                          String email,
                                          UserRole role,
                                          Map<String, Object> attributes,
                                          String nameAttributeKey) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority(role.getKey())
        );

        return new CustomOAuth2User(userId, email, role, authorities, attributes, nameAttributeKey);
    }
}
