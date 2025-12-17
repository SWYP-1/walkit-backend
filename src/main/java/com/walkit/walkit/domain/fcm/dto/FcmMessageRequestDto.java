package com.walkit.walkit.domain.fcm.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FcmMessageRequestDto {
    private Long userId;
    private String title;
    private String body;
}