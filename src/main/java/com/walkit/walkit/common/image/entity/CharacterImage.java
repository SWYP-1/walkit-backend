package com.walkit.walkit.common.image.entity;

import com.walkit.walkit.common.image.enums.Season;
import com.walkit.walkit.common.image.enums.Weather;
import com.walkit.walkit.domain.character.enums.Grade;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class CharacterImage extends Image {

    @Enumerated(EnumType.STRING)
    private Grade grade;

}
