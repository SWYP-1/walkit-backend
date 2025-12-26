package com.walkit.walkit.domain.item.controller;

import com.walkit.walkit.domain.item.dto.response.ResponseItemDto;
import com.walkit.walkit.domain.item.dto.response.ResponseMyItemDto;
import com.walkit.walkit.domain.item.enums.Position;
import com.walkit.walkit.domain.item.service.ItemService;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @PostMapping("/{itemId}")
    public ResponseEntity<Void> buy(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Long itemId) {
        itemService.buy(userPrincipal.getUserId(), itemId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<ResponseItemDto>> findAll(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam(name = "position", required = false) Position position) {
        List<ResponseItemDto> dtos = itemService.findAll(userPrincipal, position);
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    @GetMapping("/me")
    public ResponseEntity<List<ResponseMyItemDto>> findItemsByUser(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam(name = "position", required = false) Position position) {
        List<ResponseMyItemDto> dtos = itemService.findItemsByUser(userPrincipal.getUserId(), position);
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }
}
