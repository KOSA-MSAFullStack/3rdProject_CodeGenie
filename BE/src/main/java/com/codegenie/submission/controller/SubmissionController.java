package com.codegenie.submission.controller;

import com.codegenie.member.entity.MemberEntity;
import com.codegenie.member.repository.MemberRepository;
import com.codegenie.submission.dto.SubmissionDto.SubmitRequest;
import com.codegenie.submission.dto.SubmissionDto.SubmitResponse;
import com.codegenie.submission.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final MemberRepository memberRepo;

    private MemberEntity current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth != null ? auth.getName() : null);
        return memberRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("로그인 필요"));
    }

    @PostMapping("/judge")
    public SubmitResponse submit(@RequestBody SubmitRequest req) {
        return submissionService.judge(current(), req);
    }
}
