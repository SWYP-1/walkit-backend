package com.walkit.walkit.domain.walk.dto.response;

import com.walkit.walkit.domain.character.dto.response.ResponseCharacterDto;
import com.walkit.walkit.domain.character.enums.Grade;
import com.walkit.walkit.domain.walk.entity.Walk;
import lombok.Builder;
import lombok.Getter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Getter
@Builder
public class FollowerWalkResponseDto {

    private ResponseCharacterDto characterDto;
    private String walkProgressPercentage;

    private Long walkId;
    private LocalDateTime createdDate;
    private Integer stepCount;
    private Double totalDistance;

    private boolean isLiked;
    private int likeCount;

    public static FollowerWalkResponseDto from(ResponseCharacterDto characterDto, String walkProgressPercentage, Walk walk, boolean liked, int likeCount) {
        return FollowerWalkResponseDto.builder()
                .characterDto(characterDto)
                .walkProgressPercentage(walkProgressPercentage)
                .walkId(walk.getId())
                .createdDate(walk.getCreatedDate())
                .stepCount(walk.getStepCount())
                .totalDistance(walk.getTotalDistance())
                .isLiked(liked)
                .likeCount(likeCount)
                .build();
    }
}
