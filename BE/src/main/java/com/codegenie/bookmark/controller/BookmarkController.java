// BookmarkController.java
// 북마크 관련 API 요청 처리
/*
 * 설명:
 * - 북마크 관련 HTTP 요청(북마크 토글, 저장된 북마크 조회 등)을 받아 처리하는 컨트롤러
 *
 * 주요 기능:
 * - 북마크 토글 API
 * - 저장된 북마크 목록 조회 API
 */

 package com.codegenie.bookmark.controller;

import com.codegenie.bookmark.dto.BookmarkDTO;
import com.codegenie.bookmark.dto.BookmarkedQuizViewDTO;
import com.codegenie.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// * author: 김기성
@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * 지정된 퀴즈의 북마크 상태 토글(추가/해제)
     * @param quizId 북마크할 퀴즈의 ID
     * @return 성공적으로 처리되었음 : 200 OK 응답
     */
    @PostMapping("/{quizId}")
    public ResponseEntity<Void> toggleBookmark(@PathVariable Integer quizId) {
        bookmarkService.toggleBookmark(quizId);
        return ResponseEntity.ok().build();
    }

    /**
     * 현재 로그인한 사용자가 북마크한 모든 퀴즈 조회
     * @return 북마크된 퀴즈와 마지막 제출 기록이 담긴 DTO 리스트
     */
    @GetMapping("/saved")
    public ResponseEntity<List<BookmarkDTO>> getSavedBookmarks() {
        return ResponseEntity.ok(bookmarkService.getBookmarks());
    }

    /**
     * 특정 문제집에 속한 모든 퀴즈를 북마크 상태와 함께 조회
     * @param workbookId 조회할 문제집의 ID
     * @return 북마크 상태가 포함된 퀴즈 뷰 DTO 목록
     */
    @GetMapping("/workbook/{workbookId}/quizzes")
    public ResponseEntity<List<BookmarkedQuizViewDTO>> getQuizzesForWorkbook(@PathVariable Integer workbookId) {
        return ResponseEntity.ok(bookmarkService.getQuizzesForWorkbook(workbookId));
    }

    /**
     * 현재 로그인한 사용자가 북마크한 퀴즈가 있는 모든 문제집의 주제(topic) 목록 조회
     * @return 북마크된 퀴즈가 있는 문제집 주제 목록
     */
    @GetMapping("/bookmarked-topics")
    public ResponseEntity<List<String>> getWorkbookTopicsWithBookmarks() {
        return ResponseEntity.ok(bookmarkService.getWorkbookTopicsWithBookmarks());
    }
}
