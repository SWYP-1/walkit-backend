package com.walkit.walkit.global.security.jwt;

import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.enums.UserRole;
import com.walkit.walkit.common.enums.AuthProvider;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@Builder
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String email;
    private final String name;
    private final AuthProvider authProvider;
    private final UserRole role;

    public static UserPrincipal from(User user) {
        return UserPrincipal.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .authProvider(user.getAuthProvider())
                .role(user.getRole())
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority(role.getKey())
        );
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
