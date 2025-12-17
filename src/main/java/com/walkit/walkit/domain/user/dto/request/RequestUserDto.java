package com.walkit.walkit.domain.user.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.walkit.walkit.domain.user.enums.Sex;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Setter
@Getter
@RequiredArgsConstructor // final 필드를 매개변수로 받는 생성자 생성
public class RequestUserDto {

    @Pattern(regexp = "^[가-힣a-zA-Z]{1,20}$", message = "닉네임은 1~20자의 한글 또는 영문만 가능합니다.")
    private final String nickname;

    @NotNull(message = "생년월일은 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate birthDate;

    private final Sex sex;

}