// BookmarkService.java
// [서비스] 북마크 비즈니스 로직 인터페이스
/*
 * 설명:
 * - 북마크 관련 비즈니스 로직을 정의하는 인터페이스
 *
 * 주요 기능:
 * - 북마크 토글, 저장된 퀴즈 조회 기능 명세
 */
package com.codegenie.bookmark.service;

import com.codegenie.bookmark.dto.BookmarkDetailDTO;

import java.util.List;

public interface BookmarkService {

    /**
     * 북마크 상태를 토글(추가/해제)
     * @param quizId 토글할 문제의 ID
     */
    void toggleBookmark(Integer quizId);

    /**
     * 현재 로그인한 사용자가 북마크한 모든 퀴즈를 조회
     * @return 북마크된 퀴즈와 제출 기록이 담긴 DTO 목록
     */
    List<BookmarkDetailDTO> getSavedQuizzes();
}
