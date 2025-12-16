package com.walkit.walkit.domain.user.dto.request;

import com.walkit.walkit.domain.user.enums.Sex;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class RequestUserDto {

    private String nickname;
    private LocalDate birthDate;
    private Sex sex;
}
