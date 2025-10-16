// BookmarkController.java
// [컨트롤러] 북마크 관련 API 요청 처리
/*
 * 설명:
 * - 북마크 토글, 저장된 북마크 조회 등 북마크 관련 HTTP 요청을 받아 처리하는 컨트롤러
 *
 * 주요 기능:
 * - 북마크 토글 API
 * - 저장된 북마크 목록 조회 API
 */
package com.codegenie.bookmark.controller;

import com.codegenie.bookmark.dto.BookmarkDetailDTO;
import com.codegenie.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * 지정된 퀴즈의 북마크 상태를 토글(추가/해제)
     * @param quizId 북마크할 퀴즈의 ID
     * @return 성공적으로 처리되었음을 나타내는 200 OK 응답
     */
    @PostMapping("/{quizId}")
    public ResponseEntity<Void> toggleBookmark(@PathVariable Integer quizId) {
        bookmarkService.toggleBookmark(quizId);
        return ResponseEntity.ok().build();
    }

    /**
     * 현재 로그인한 사용자가 저장한 모든 북마크를 조회
     * @return 북마크된 퀴즈와 마지막 제출 기록이 담긴 DTO 리스트
     */
    @GetMapping("/saved")
    public ResponseEntity<List<BookmarkDetailDTO>> getSavedBookmarks() {
        List<BookmarkDetailDTO> savedQuizzes = bookmarkService.getSavedQuizzes();
        return ResponseEntity.ok(savedQuizzes);
    }
}