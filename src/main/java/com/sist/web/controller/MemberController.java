package com.sist.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.dto.MemberResponse;
import com.sist.web.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMe(Authentication authentication) {
        // SecurityConfig에서 인증된 요청만 도달 — authentication은 항상 존재
        return ResponseEntity.ok(memberService.getMe(authentication));
    }


}
