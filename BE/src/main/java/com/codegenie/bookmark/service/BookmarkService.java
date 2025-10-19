// BookmarkService.java
// 북마크 비즈니스 로직 인터페이스
/*
 * 설명:
 * - 북마크 관련 비즈니스 로직을 정의하는 인터페이스
 *
 * 주요 기능:
 * - 북마크 토글, 저장된 퀴즈 조회, 문제집 내 퀴즈 조회 기능 명세
 * - 북마크된 퀴즈가 있는 문제집 주제 목록 조회 기능 명세
 */

package com.codegenie.bookmark.service;

import com.codegenie.bookmark.dto.BookmarkDTO;
import com.codegenie.bookmark.dto.BookmarkedQuizViewDTO;
import java.util.List;

// * author: 김기성
public interface BookmarkService {
    /**
     * 북마크 상태 토글(추가/해제)
     * @param quizId 토글할 문제의 ID
     */
    void toggleBookmark(Integer quizId);

    /**
     * 현재 로그인한 사용자가 북마크한 모든 퀴즈 조회
     * @return 북마크된 퀴즈와 마지막 제출 기록이 담긴 DTO 목록
     */
    List<BookmarkDTO> getBookmarks();

    /**
     * 특정 문제집에 속한 모든 퀴즈 북마크 상태와 함께 조회
     * @param workbookId 조회할 문제집의 ID
     * @return 북마크 상태가 포함된 퀴즈 뷰 DTO 목록
     */
    List<BookmarkedQuizViewDTO> getQuizzesForWorkbook(Integer workbookId);

    /**
     * 현재 로그인한 사용자가 북마크한 퀴즈가 있는 모든 문제집의 주제(topic) 목록 조회
     * @return 북마크된 퀴즈가 있는 문제집 주제 목록
     */
    List<String> getWorkbookTopicsWithBookmarks();
}
