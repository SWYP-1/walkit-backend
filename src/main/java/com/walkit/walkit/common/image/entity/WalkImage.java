package com.walkit.walkit.common.image.entity;

import com.walkit.walkit.domain.walk.entity.Walk;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class WalkImage extends Image {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "walk_id")
    private Walk walk;

    @Builder
    public WalkImage(Long id, String imageName, Walk walk) {
        super(id, imageName);
        this.walk = walk;
    }
}
