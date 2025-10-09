// SubmissionController.java
// [컨트롤러] 문제 제출 관련 API 요청 처리
/*
 * 설명:
 * - 답안 제출 등 문제 제출 관련 HTTP 요청을 받아 처리하는 컨트롤러
 * 
 * 주요 기능:
 * - 답안 제출 API
 */
package com.codegenie.submission.controller;

import com.codegenie.submission.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    // 답안 제출을 위한 POST 요청을 처리
    @PostMapping
    public ResponseEntity<Void> submitAnswer(@RequestBody Map<String, Object> payload) {
        // TODO: Spring Security 도입 후, 인증된 사용자 정보에서 memberId를 가져오도록 수정 필요
        Long memberId = 1L; // 임시로 사용자 ID를 1로 하드코딩
        Long quizId = ((Number) payload.get("quizId")).longValue();
        String answer = (String) payload.get("answer");

        submissionService.submitAnswer(memberId, quizId, answer);
        return ResponseEntity.ok().build();
    }
}
