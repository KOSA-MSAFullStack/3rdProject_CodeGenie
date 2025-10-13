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

import com.codegenie.member.dto.CustomUserDetails;
import com.codegenie.submission.dto.SubmissionRequestDto;
import com.codegenie.submission.dto.SubmissionResponseDto;
import com.codegenie.submission.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// * author: 김기성
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/submission")
    public ResponseEntity<SubmissionResponseDto> submitAnswer(
            @RequestBody SubmissionRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 1. 사용자 인증 정보 확인
        if (userDetails == null) {
            // 인증되지 않은 사용자에 대한 예외 처리 또는 에러 응답
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        // 2. 서비스 호출
        Integer memberId = userDetails.getMemberId();
        SubmissionResponseDto responseDto = submissionService.submitAnswer(requestDto, memberId);

        // 3. 결과 반환
        return ResponseEntity.ok(responseDto);
    }
}
