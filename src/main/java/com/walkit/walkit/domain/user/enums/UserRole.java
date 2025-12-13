package com.walkit.walkit.domain.user.enums;

public enum UserRole {
    ROLE_USER,
    ROLE_ADMIN;

    public String getKey() {
        return this.name();
    }
}
