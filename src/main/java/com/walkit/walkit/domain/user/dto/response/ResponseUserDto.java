package com.walkit.walkit.domain.user.dto.response;

import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.enums.Sex;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ResponseUserDto {

    private Long userId;
    private String imageName;
    private String nickname;
    private LocalDate birthDate;
    private Sex sex;

    public static ResponseUserDto from(String imageName, User user) {
        return ResponseUserDto.builder()
                .userId(user.getId())
                .imageName(imageName)
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .sex(user.getSex())
                .build();
    }
}
