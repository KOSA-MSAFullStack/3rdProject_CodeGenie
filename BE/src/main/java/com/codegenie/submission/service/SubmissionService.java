// SubmissionService.java
// [서비스] 문제 제출 비즈니스 로직 인터페이스
/*
 * 설명:
 * - 문제 제출 관련 비즈니스 로직을 정의하는 인터페이스
 * 
 * 주요 기능:
 * - 답안 제출 및 채점 요청 기능 명세
 */
package com.codegenie.submission.service;

import com.codegenie.submission.dto.SubmissionRequestDto;
import com.codegenie.submission.dto.SubmissionResponseDto;

// * author: 김기성
public interface SubmissionService {

    /**
     * 답안을 제출하고 Judge0을 통해 채점을 요청
     * @param requestDto 제출 요청 데이터 (코드, 언어, 문제 ID)
     * @param memberId 현재 로그인한 사용자의 ID
     * @return SubmissionResponseDto 채점 결과 데이터
     */
    SubmissionResponseDto submitAnswer(SubmissionRequestDto requestDto, Integer memberId);
}
