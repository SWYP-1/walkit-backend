package com.walkit.walkit.domain.page.controller;

import com.walkit.walkit.domain.page.dto.ResponseHomeDto;
import com.walkit.walkit.domain.page.service.PageService;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pages")
public class PageController {

    private final PageService pageService;

    @GetMapping("/home")
    public ResponseEntity<ResponseHomeDto> getHomePage(@AuthenticationPrincipal UserPrincipal userPrincipal,
         @RequestParam double lat, @RequestParam double lon
    ) {
        ResponseHomeDto dto = pageService.getHomePage(userPrincipal.getUserId(), lat, lon);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }
}
