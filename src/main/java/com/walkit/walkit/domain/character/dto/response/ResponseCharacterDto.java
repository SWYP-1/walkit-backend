package com.walkit.walkit.domain.character.dto.response;

import com.walkit.walkit.domain.character.entity.Character;
import com.walkit.walkit.domain.character.enums.Grade;
import com.walkit.walkit.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseCharacterDto {

    private String headImageName;
    private String bodyImageName;
    private String feetImageName;
    private String characterImageName;
    private String backgroundImageName;
    private int level;
    private Grade grade;
    private String nickName;
    private int currentGoalSequence;

    public static ResponseCharacterDto from(Character character, User user, String characterImageName, String backgroundImage) {
        return ResponseCharacterDto.builder()
                .headImageName(character.getHeadImageName())
                .bodyImageName(character.getBodyImageName())
                .feetImageName(character.getFeetImageName())
                .characterImageName(characterImageName)
                .backgroundImageName(backgroundImage)
                .level(character.getLevel())
                .grade(character.getGrade())
                .nickName(user.getNickname())
                .build();
    }

    public static ResponseCharacterDto from(Character character, User user, String characterImageName, String backgroundImage, int currentGoalSequence) {
        return ResponseCharacterDto.builder()
                .headImageName(character.getHeadImageName())
                .bodyImageName(character.getBodyImageName())
                .feetImageName(character.getFeetImageName())
                .characterImageName(characterImageName)
                .backgroundImageName(backgroundImage)
                .level(character.getLevel())
                .grade(character.getGrade())
                .nickName(user.getNickname())
                .currentGoalSequence(currentGoalSequence)
                .build();
    }
}
