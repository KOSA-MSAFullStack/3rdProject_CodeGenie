// BookmarkController.java
// [컨트롤러] 북마크 관련 API 요청 처리
/*
 * 설명:
 * - 북마크 생성, 조회, 삭제 등 북마크 관련 HTTP 요청을 받아 처리하는 컨트롤러
 * 
 * 주요 기능:
 * - 북마크 생성 API
 */
package com.codegenie.bookmark.controller;

import com.codegenie.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // 북마크 생성을 위한 POST 요청을 처리
    @PostMapping
    public ResponseEntity<Void> createBookmark(@RequestBody Map<String, Long> payload) {
        // TODO: Spring Security 도입 후, 인증된 사용자 정보에서 memberId를 가져오도록 수정 필요
        Long memberId = 1L; // 임시로 사용자 ID를 1로 하드코딩
        Long quizId = payload.get("quizId");

        bookmarkService.createBookmark(memberId, quizId);
        return ResponseEntity.ok().build();
    }
}
