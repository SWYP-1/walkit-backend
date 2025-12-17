package com.walkit.walkit.domain.follow.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FollowStatus {

    EMPTY,
    PENDING,
    ACCEPTED,
    REJECTED,
    MYSELF
}
