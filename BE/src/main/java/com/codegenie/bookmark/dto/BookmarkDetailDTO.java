package com.codegenie.bookmark.dto;

import com.codegenie.workbook.dto.QuizResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** 북마크된 문제 + 마지막 제출 한 건(있으면) */
@Getter
@Builder
public class BookmarkDetailDTO {
    private QuizResponse quiz;
    private LastSubmission lastSubmission;

    @Getter
    @Builder
    public static class LastSubmission {
        private String status;            // "Accepted" / "Wrong Answer" / ...
        private LocalDateTime submittedAt;
        private String answer;            // 사용자가 제출한 코드
    }
}
