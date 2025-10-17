package com.codegenie.bookmark.controller;

import com.codegenie.bookmark.dto.BookmarkDetailDTO;
import com.codegenie.bookmark.dto.BookmarkedQuizViewDTO;
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

    /** 지정된 퀴즈의 북마크 상태 토글(추가/해제) */
    @PostMapping("/{quizId}")
    public ResponseEntity<Void> toggleBookmark(@PathVariable Integer quizId) {
        bookmarkService.toggleBookmark(quizId);
        return ResponseEntity.ok().build();
    }

    /** 현재 로그인 사용자가 북마크한 모든 퀴즈 + 마지막 제출 기록 */
    @GetMapping("/saved")
    public ResponseEntity<List<BookmarkDetailDTO>> getSavedBookmarks() {
        return ResponseEntity.ok(bookmarkService.getBookmarks());
    }

    /** 특정 문제집의 퀴즈들을 북마크 상태와 함께 조회 */
    @GetMapping("/workbook/{workbookId}/quizzes")
    public ResponseEntity<List<BookmarkedQuizViewDTO>> getQuizzesForWorkbook(@PathVariable Integer workbookId) {
        return ResponseEntity.ok(bookmarkService.getQuizzesForWorkbook(workbookId));
    }

    /** 북마크된 퀴즈가 존재하는 문제집 주제(topic) 목록 */
    @GetMapping("/bookmarked-topics")
    public ResponseEntity<List<String>> getWorkbookTopicsWithBookmarks() {
        return ResponseEntity.ok(bookmarkService.getWorkbookTopicsWithBookmarks());
    }
}
