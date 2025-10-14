package com.codegenie.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codegenie.member.dto.CustomUserDetails;
import com.codegenie.member.dto.MemberInfoDTO;
import com.codegenie.member.dto.MemberUpdateDTO;
import com.codegenie.member.service.MemberService;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 마이페이지 진입 시 현재 회원정보 조회
    @GetMapping("/info")
    public MemberInfoDTO getMemberInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return memberService.getMemberInfo(userDetails.getUsername());
    }

    // 이름 또는 비밀번호 변경
    @PutMapping("/update")
    public ResponseEntity<?> updateMember(@RequestBody MemberUpdateDTO dto,
                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            memberService.updateMember(userDetails.getUsername(), dto);
            return ResponseEntity.ok("success");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
