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

    private LocalDateTime createdDate;
    private Integer stepCount;
    private Double totalDistance;

    public static FollowerWalkResponseDto from(ResponseCharacterDto characterDto, String walkProgressPercentage, Walk walk) {
        return FollowerWalkResponseDto.builder()
                .characterDto(characterDto)
                .walkProgressPercentage(walkProgressPercentage)
                .createdDate(walk.getCreatedDate())
                .stepCount(walk.getStepCount())
                .totalDistance(walk.getTotalDistance())
                .build();
    }
}
