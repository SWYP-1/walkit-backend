package com.walkit.walkit.domain.walkLike.controller;

import com.walkit.walkit.domain.walkLike.service.WalkLikeService;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/walk-likes")
@RequiredArgsConstructor
public class WalkLikeController {

    private final WalkLikeService walkLikeService;

    @PostMapping("/{walkId}")
    public ResponseEntity<Void> save(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Long walkId) {
        walkLikeService.save(userPrincipal.getUserId(), walkId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{walkId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Long walkId) {
        walkLikeService.delete(userPrincipal.getUserId(), walkId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
