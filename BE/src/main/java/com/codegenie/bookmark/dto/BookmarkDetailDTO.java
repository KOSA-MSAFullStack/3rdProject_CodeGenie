// BookmarkDetailDTO.java
// [DTO] 북마크 상세 정보 전송
/*
 * 설명:
 * - 북마크된 문제의 상세 정보와 해당 문제에 대한 마지막 제출 기록을 함께 담는 DTO
 * - '저장한 문제' 탭에서 사용됨
 *
 * 주요 기능:
 * - 퀴즈 정보와 마지막 제출 기록 클라이언트로 전달
 */
package com.codegenie.bookmark.dto;

import com.codegenie.submission.dto.SubmissionResponseDto;
import com.codegenie.workbook.dto.QuizResponse;
import lombok.Builder;
import lombok.Getter;

// * author: 김기성
@Getter
@Builder
public class BookmarkDetailDTO {
    private QuizResponse quiz;                      // 북마크된 문제 정보
    private SubmissionResponseDto lastSubmission;   // 해당 문제에 대한 사용자의 마지막 제출 기록 (없을 경우 null)
}
