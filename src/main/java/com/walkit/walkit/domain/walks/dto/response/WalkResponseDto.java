package com.walkit.walkit.domain.walks.dto.response;

import com.walkit.walkit.domain.walks.entity.Emotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalkResponseDto {
    private Long id;
    private Emotion emotion;
    private String text;
    private String imageUrl;
    private LocalDate walkedAt;
    private LocalDateTime createdAt;
}
