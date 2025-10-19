// SubmissionHistoryDTO.java
// 제출 기록 조회 DTO
/*
 * 설명:
 * - 특정 문제에 대한 사용자의 이전 제출 기록을 조회할 때 사용되는 DTO
 * - 각 제출 건의 상세 정보를 담아 클라이언트로 전달
 *
 * 주요 기능:
 * - 제출 기록 목록 조회 시 개별 항목의 데이터 표현
 */

package com.codegenie.submission.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

// * author: 김기성
@Getter
@Builder
// 제출 기록 정보 담는 DTO
public class SubmissionHistoryDTO {
    private Integer id;                 // 제출 ID
    private String status;              // 채점 상태
    private String answer;              // 제출한 답안
    private String stdout;              // 표준 출력
    private String stderr;              // 표준 에러
    private Double time;                // 실행 시간 (초)
    private Integer memory;             // 메모리 사용량 (KB)
    private LocalDateTime submittedAt;  // 제출 시간
}