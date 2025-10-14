// SubmissionResponseDto.java
// [DTO] 문제 제출 결과 전송,'서버->클라이언트로 데이터 응답 객체'
/*
 * 설명:
 * - 서버에서 클라이언트로 채점 결과를 전송할 때 사용하는 DTO
 * - 채점 상태, 실행 결과, 리소스 사용량 등 프론트엔드에 표시될 정보를 포함
 */

package com.codegenie.submission.dto;

import lombok.*;
import java.time.LocalDateTime;

// * author: 김기성
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionResponseDto {
    private Integer submissionId;       // 제출 ID (PK)
    private Integer quizId;             // 문제 ID (FK)
    private String answer;              // 제출된 소스 코드
    private String language;            // 사용 언어
    private String status;              // 채점 상태
    private String stdout;              // 표준 출력
    private String stderr;              // 표준 에러
    private Double runTime;             // 실행 시간
    private Integer memory;             // 사용 메모리 (KB)
    private LocalDateTime submittedAt;  // 제출 시간
}
