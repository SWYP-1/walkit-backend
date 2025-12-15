package com.walkit.walkit.domain.goal.controller;

import com.walkit.walkit.domain.goal.dto.request.RequestGoalDto;
import com.walkit.walkit.domain.goal.dto.response.ResponseGoalDto;
import com.walkit.walkit.domain.goal.dto.response.ResponseGoalProcessDto;
import com.walkit.walkit.domain.goal.service.GoalService;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public ResponseEntity<Void> saveGoal(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody RequestGoalDto dto) {
        goalService.saveGoal(userPrincipal.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<ResponseGoalDto> findGoal(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        ResponseGoalDto dto = goalService.findGoal(userPrincipal.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @GetMapping("/progress")
    public ResponseEntity<ResponseGoalProcessDto> findGoalProcess(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        ResponseGoalProcessDto dto = goalService.findGoalProcess(userPrincipal.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PutMapping
    public ResponseEntity<Void> updateGoal(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody RequestGoalDto dto) {
        goalService.updateGoal(userPrincipal.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
