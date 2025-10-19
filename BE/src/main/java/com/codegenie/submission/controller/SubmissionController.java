// SubmissionController.java
// 코드 제출 및 기록 조회 API
/*
 * 설명:
 * - 사용자가 제출한 코드 채점, 특정 문제에 대한 제출 기록 조회하는 API 제공
 *
 * 주요 기능:
 * - 코드 채점 요청 처리
 * - 특정 문제에 대한 사용자별 제출 기록 목록 조회
 */

package com.codegenie.submission.controller;

import com.codegenie.member.entity.MemberEntity;
import com.codegenie.member.repository.MemberRepository;
import com.codegenie.submission.dto.SubmissionDTO.SubmitRequest;
import com.codegenie.submission.dto.SubmissionDTO.SubmitResponse;
import com.codegenie.submission.dto.SubmissionHistoryDTO;
import com.codegenie.submission.entity.Submission;
import com.codegenie.submission.repository.SubmissionRepository;
import com.codegenie.submission.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

// * author: 김기성
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final MemberRepository memberRepo;
    private final SubmissionRepository submissionRepository;

    // 현재 로그인된 사용자 정보 조회
    private MemberEntity current() {
        // 인증 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 인증 정보 o -> 사용자 이메일 가져오기 (x -> null)
        String email = (auth != null ? auth.getName() : null);
        // 이메일로 회원 정보 조회 (x -> 예외 발생)
        return memberRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("로그인 필요"));
    }

    // POST /api/submissions/judge: 코드 채점 요청
    @PostMapping("/judge")
    public SubmitResponse submit(@RequestBody SubmitRequest req) {
        // 서비스에 채점 위임
        return submissionService.judge(current(), req);
    }

    // GET /api/submissions?quizId={quizId}: 특정 문제에 대한 제출 기록 조회
    @GetMapping
    public List<SubmissionHistoryDTO> listByQuiz(@RequestParam("quizId") Integer quizId) {
        // 현재 사용자 정보 조회
        MemberEntity me = current();

        // 사용자 & 문제 ID로 제출 기록 최신순 조회
        List<Submission> rows =
                submissionRepository.findByMemberIdAndQuizIdOrderBySubmittedAtDesc(me.getMember_id(), quizId);

        // 조회된 제출 기록 SubmissionHistoryDTO로 변환, 리스트로 반환
        return rows.stream()
                .map(s -> SubmissionHistoryDTO.builder()
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
