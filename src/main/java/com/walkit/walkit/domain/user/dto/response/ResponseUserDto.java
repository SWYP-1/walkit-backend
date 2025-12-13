package com.walkit.walkit.domain.user.dto.response;

import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.enums.Sex;
import com.walkit.walkit.common.enums.AuthProvider;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ResponseUserDto {

    private String imageName;
    private String name;
    private LocalDate birthDate;
    private Sex sex;
    private AuthProvider provider;

    public static ResponseUserDto from(String imageName, User user) {
        return ResponseUserDto.builder()
                .imageName(imageName)
                .name(user.getName())
//                .birthDate(user.getBirthYear())
                .sex(user.getSex())
                .provider(user.getAuthProvider())
                .build();
    }
}
