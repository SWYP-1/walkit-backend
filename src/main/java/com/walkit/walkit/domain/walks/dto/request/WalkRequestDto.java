package com.walkit.walkit.domain.walks.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.walkit.walkit.domain.walks.entity.Emotion;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class WalkRequestDto {

    private Emotion emotion;        // 감정
    private String text;            // 산책 일기 텍스트
    private LocalDate walkedAt;     // 산책 날짜
    private List<WalkPointRequestDto> points;    // 코스(선택) - 없으면 null
    @JsonIgnore
    private MultipartFile image;    // 이미지


}
