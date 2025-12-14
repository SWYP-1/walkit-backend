package com.walkit.walkit.domain.character.controller;

import com.walkit.walkit.domain.character.service.CharacterService;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/characters")
public class CharacterController {

    private final CharacterService characterService;

    @GetMapping("/imageName")
    public String getCharacterImageName(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return characterService.getCharacterImageName(userPrincipal.getUserId());
    }
}
