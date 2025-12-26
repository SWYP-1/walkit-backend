package com.walkit.walkit.domain.follow.controller;

import com.walkit.walkit.domain.follow.dto.response.ResponseFollowerDto;
import com.walkit.walkit.domain.follow.dto.response.ResponseFollowingDto;
import com.walkit.walkit.domain.follow.service.FollowService;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/follows")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/following/nickname/{nickname}")
    public ResponseEntity<Void> sendFollowing(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable String nickname) {
        followService.sendFollowing(userPrincipal.getUserId(), nickname);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<ResponseFollowerDto>> findFollow(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ResponseFollowerDto> dto = followService.findFollow(userPrincipal.getUserId());
        return  ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @GetMapping("/follower")
    public ResponseEntity<List<ResponseFollowingDto>> findRequestFollower(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ResponseFollowingDto> dto = followService.findRequestFollower(userPrincipal.getUserId());
        return  ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PatchMapping("/nickname/{nickname}")
    public ResponseEntity<Void> acceptFollow(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable String nickname) {
        followService.acceptFollow(userPrincipal.getUserId(), nickname);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/nickname/{nickname}")
    public ResponseEntity<Void> deleteFollow(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable String nickname) {
        followService.deleteFollow(userPrincipal.getUserId(), nickname);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/follower/nickname/{nickname}")
    public ResponseEntity<Void> deleteFollower(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable String nickname) {
        followService.deleteFollower(userPrincipal.getUserId(), nickname);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
