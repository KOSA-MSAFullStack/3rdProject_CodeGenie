// BookmarkDTO.java
// 북마크 상세 정보 전송
/*
 * 설명:
 * - 북마크된 문제의 상세 정보와 해당 문제에 대한 마지막 제출 기록을 함께 담는 DTO
 * - '저장한 문제' 탭에서 사용됨
 *
 * 주요 기능:
 * - 퀴즈 정보와 마지막 제출 기록 클라이언트로 전달
 */

 package com.codegenie.bookmark.dto;

import com.codegenie.workbook.dto.QuizResponse;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

// * author: 김기성
@Getter
@Builder
public class BookmarkDTO {
    private QuizResponse quiz;              // 북마크된 문제 상세 정보
    private LastSubmission lastSubmission;  // 해당 문제에 대한 사용자의 마지막 제출 기록 (없을 경우 null)

    @Getter
    @Builder
    // 북마크된 문제 + 마지막 제출 한 건(있으면)
    public static class LastSubmission {
        private String answer;              // 사용자가 제출한 코드
        private String status;              //채점 상태 (Accepted/Wrong Answer/...)
        private LocalDateTime submittedAt;  // 제출 시간
    }
}
