package com.walkit.walkit.domain.character.dto.response;

import com.walkit.walkit.domain.character.entity.Character;
import com.walkit.walkit.domain.character.enums.Grade;
import com.walkit.walkit.domain.item.enums.Position;
import com.walkit.walkit.domain.item.enums.Tag;
import com.walkit.walkit.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Builder
public class ResponseCharacterDto {

    private ResponseCharacterWearDto headImage;
    private ResponseCharacterWearDto bodyImage;
    private ResponseCharacterWearDto feetImage;

    private String characterImageName;
    private String backgroundImageName;
    private int level;
    private Grade grade;
    private String nickName;
    private int currentGoalSequence;



    public static ResponseCharacterDto from(Character character, User user, String characterImageName, String backgroundImage, Tag headTag, String feetImage) {
        return ResponseCharacterDto.builder()
                .headImage(ResponseCharacterWearDto.builder().imageName(character.getHeadImageName()).itemPosition(Position.HEAD).itemTag(headTag).build())
                .bodyImage(ResponseCharacterWearDto.builder().imageName(character.getBodyImageName()).itemPosition(Position.BODY).build())
                .feetImage(ResponseCharacterWearDto.builder().imageName(feetImage).itemPosition(Position.FEET).build())
                .characterImageName(characterImageName)
                .backgroundImageName(backgroundImage)
                .level(character.getLevel())
                .grade(character.getGrade())
                .nickName(user.getNickname())
                .build();
    }

    public static ResponseCharacterDto from(Character character, User user, String characterImageName, String backgroundImage, int currentGoalSequence, Tag headTag, String feetImage) {
        return ResponseCharacterDto.builder()
                .headImage(ResponseCharacterWearDto.builder().imageName(character.getHeadImageName()).itemPosition(Position.HEAD).itemTag(headTag).build())
                .bodyImage(ResponseCharacterWearDto.builder().imageName(character.getBodyImageName()).itemPosition(Position.BODY).build())
                .feetImage(ResponseCharacterWearDto.builder().imageName(feetImage).itemPosition(Position.FEET).build())
                .characterImageName(characterImageName)
                .backgroundImageName(backgroundImage)
                .level(character.getLevel())
                .grade(character.getGrade())
                .nickName(user.getNickname())
                .currentGoalSequence(currentGoalSequence)
                .build();
    }
}
