package com.walkit.walkit.domain.user.entity;

public enum UserRole {
    ROLE_USER,
    ROLE_ADMIN;

    public String getKey() {
        return this.name();
    }
}
