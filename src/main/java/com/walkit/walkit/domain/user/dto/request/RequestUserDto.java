package com.walkit.walkit.domain.user.dto.request;

import com.walkit.walkit.domain.user.enums.Sex;

import java.time.LocalDate;

public class RequestUserDto {

    private String name;
    private LocalDate birthDate;
    private Sex sex;
}
