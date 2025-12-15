package com.walkit.walkit.domain.character.entity;

import com.walkit.walkit.domain.user.enums.Asset;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Character {

    private int experience = 0;
    private int level = 1;
    private Asset asset;
}
