package com.codegenie.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SubmissionDto {

    @Data
    public static class SubmitRequest {
        private Integer quizId;
        private String answer;
        private String language;  // "Java" | "Python" | "C++"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitResponse {
        private String status;     // "Accepted" | "Wrong Answer" | "Error"
        private String stdout;     // 표준출력
        private String stderr;     // 표준에러(컴파일/런타임 메시지 등)
        private Double time;       // 실행 시간(s) - 간이 채점은 null
        private Integer memory;    // 메모리(KB) - 간이 채점은 null

        // ✅ 프론트 실시간 카운터 반영용 절대값
        private Long submissions;  // 총 제출 횟수
        private Long accepted;     // 총 정답 횟수
    }
}
