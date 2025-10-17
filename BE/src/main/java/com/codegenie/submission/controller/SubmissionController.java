package com.codegenie.submission.controller;

import com.codegenie.member.entity.MemberEntity;
import com.codegenie.member.repository.MemberRepository;
import com.codegenie.submission.dto.SubmissionDto.SubmitRequest;
import com.codegenie.submission.dto.SubmissionDto.SubmitResponse;
import com.codegenie.submission.dto.SubmissionHistoryDto;
import com.codegenie.submission.entity.SubmissionEntity;
import com.codegenie.submission.repository.SubmissionRepository;
import com.codegenie.submission.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;   // ✅ 추가

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final MemberRepository memberRepo;
    private final SubmissionRepository submissionRepository;

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

    @GetMapping
    public List<SubmissionHistoryDto> listByQuiz(@RequestParam("quizId") Integer quizId) {
        MemberEntity me = current();

        List<SubmissionEntity> rows =
                submissionRepository.findByMemberIdAndQuizIdOrderBySubmittedAtDesc(me.getMember_id(), quizId);

        return rows.stream()
                .<SubmissionHistoryDto>map(s -> SubmissionHistoryDto.builder()
                        .id(s.getId())
                        .status(s.getStatus())
                        .answer(s.getAnswer())
                        .stdout(s.getStdout())
                        .stderr(s.getStderr())
                        .time(s.getRunTime())
                        .memory(s.getMemory())
                        .submittedAt(s.getSubmittedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
