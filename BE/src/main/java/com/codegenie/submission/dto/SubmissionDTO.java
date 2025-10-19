// SubmissionDTO.java
// 코드 제출 및 결과 DTO
/*
 * 설명:
 * - 코드 제출 요청 및 채점 결과 응답에 사용되는 데이터 전송 객체(DTO)들 정의
 *
 * 주요 기능:
 * - SubmitRequest: 코드 제출 시 필요한 정보(문제 ID, 소스 코드, 언어) 담는 클래스
 * - SubmitResponse: 채점 결과 담는 클래스 (채점 상태, 실행 결과, 리소스 사용량 등)
 */

package com.codegenie.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// * author: 김기성
// 코드 제출 관련 DTO 컨테이너 클래스
public class SubmissionDTO {

    // 코드 제출 요청 DTO
    @Data
    public static class SubmitRequest {
        private Integer quizId;     // 문제 ID
        private String answer;      // 제출된 소스 코드
        private String language;    // 사용 언어 ("Java" | "Python" | "C++")
    }

    // 코드 채점 결과 응답 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitResponse {
        private String status;     // 채점 상태 ("Accepted" | "Wrong Answer" | "Error")
        private String stdout;     // 표준 출력
        private String stderr;     // 표준 에러 (컴파일/런타임 메시지 등)
        private Double time;       // 실행 시간 (s)
        private Integer memory;    // 메모리 사용량 (KB)
        private Long submissions;  // 총 제출 횟수
        private Long accepted;     // 총 정답 횟수
    }
}
