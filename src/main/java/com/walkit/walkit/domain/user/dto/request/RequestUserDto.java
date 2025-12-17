package com.walkit.walkit.domain.user.dto.request;

import com.walkit.walkit.domain.user.enums.Sex;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class RequestUserDto {

    @Pattern(regexp = "^[가-힣a-zA-Z]{1,20}$", message = "닉네임은 1~20자의 한글 또는 영문만 가능합니다.")
    private String nickname;

    @NotNull(message = "생년월일은 필수입니다.")
    private LocalDate birthDate;

    private Sex sex;
}
