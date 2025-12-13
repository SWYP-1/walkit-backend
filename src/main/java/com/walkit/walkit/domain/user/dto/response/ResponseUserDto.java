package com.walkit.walkit.domain.user.dto.response;

import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.enums.Sex;
import com.walkit.walkit.global.common.enums.AuthProvider;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ResponseUserDto {

    private String image;
    private String name;
    private LocalDate birthDate;
    private Sex sex;
    private AuthProvider provider;

    public static ResponseUserDto from(String image, User user) {
        return ResponseUserDto.builder()
                .name(user.getName())
//                .birthDate(user.getBirthYear())
                .sex(user.getSex())
                .provider(user.getAuthProvider())
                .build();
    }
}
