package com.walkit.walkit.domain.character.controller;

import com.walkit.walkit.domain.character.dto.request.RequestItemWearDto;
import com.walkit.walkit.domain.character.dto.response.ResponseCharacterDto;
import com.walkit.walkit.domain.character.service.CharacterService;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/characters")
public class CharacterController {

    private final CharacterService characterService;

    @GetMapping
    public ResponseEntity<ResponseCharacterDto> find(@AuthenticationPrincipal UserPrincipal userPrincipal,
        @RequestParam double lat, @RequestParam double lon
    ) {
        ResponseCharacterDto dto = characterService.find(userPrincipal.getUserId(), lat, lon);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<Void> wearOrTakeOff(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                              @RequestBody RequestItemWearDto dto, @PathVariable Long itemId
    ) {
        characterService.wearOrTakeOff(userPrincipal.getUserId(), itemId, dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
