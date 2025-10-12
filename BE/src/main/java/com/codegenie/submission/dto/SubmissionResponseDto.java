// SubmissionResponseDto.java
// [DTO] 문제 제출 결과 전송
/*
 * 설명:
 * - 서버에서 클라이언트로 채점 결과를 전송할 때 사용하는 DTO
 * - 채점 상태, 실행 결과, 리소스 사용량 등 프론트엔드에 표시될 정보를 포함
 */
package com.codegenie.submission.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionResponseDto {
    // 제출 ID (PK)
    private Integer submissionId;

    // 문제 ID (FK)
    private Integer quizId;

    // 제출된 소스 코드
    private String answer;

    // 사용 언어
    private String language;

    // 채점 상태
    private String status;

    // 표준 출력
    private String stdout;

    // 표준 에러
    private String stderr;

    // 실행 시간
    private Double runTime;

    // 사용 메모리 (KB)
    private Integer memory;

    // 제출 시간
    private LocalDateTime submittedAt;
}
