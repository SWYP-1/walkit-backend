package com.walkit.walkit.domain.follow.controller;

import com.walkit.walkit.domain.follow.dto.response.ResponseFollowerDto;
import com.walkit.walkit.domain.follow.dto.response.ResponseFollowingDto;
import com.walkit.walkit.domain.follow.entity.Follow;
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

    @PostMapping("/nickname/{nickname}")
    public ResponseEntity<Void> sendFollow(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable String nickname) {
        followService.sendFollow(userPrincipal.getUserId(), nickname);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/follower")
    public ResponseEntity<List<ResponseFollowerDto>> findFollowers(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ResponseFollowerDto> dto = followService.findFollowers(userPrincipal.getUserId());
        return  ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @GetMapping("/following")
    public ResponseEntity<List<ResponseFollowingDto>> findRequestFollowing(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ResponseFollowingDto> dto = followService.findRequestFollowing(userPrincipal.getUserId());
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

    @DeleteMapping("/following/nickname/{nickname}")
    public ResponseEntity<Void> deleteFollowing(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable String nickname) {
        followService.deleteFollowing(userPrincipal.getUserId(), nickname);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
