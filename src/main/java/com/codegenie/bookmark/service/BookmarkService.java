// BookmarkService.java
// [서비스] 북마크 비즈니스 로직 인터페이스
/*
 * 설명:
 * - 북마크 관련 비즈니스 로직을 정의하는 인터페이스
 * 
 * 주요 기능:
 * - 북마크 생성, 조회, 삭제 기능 명세
 */
package com.codegenie.bookmark.service;

import java.util.List;

import com.codegenie.bookmark.dto.SavedBookmarkDTO;

public interface BookmarkService {

    /**
     * 북마크를 생성
     * @param memberId 현재 로그인한 사용자의 ID
     * @param quizId 북마크할 문제의 ID
     */
    void createBookmark(Long memberId, Long quizId);

    /**
     * 특정 사용자의 모든 북마크 그룹과 북마크들을 조회
     * @param memberId 현재 로그인한 사용자의 ID
     * @return 해당 사용자의 모든 북마크 정보를 담은 DTO 리스트
     */
    List<SavedBookmarkDTO> getSavedBookmarks(Long memberId);
}
