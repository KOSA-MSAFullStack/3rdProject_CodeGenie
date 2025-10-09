// BookmarkServiceImpl.java
// [서비스] 북마크 비즈니스 로직 구현체
/*
 * 설명:
 * - BookmarkService 인터페이스를 구현한 클래스
 * - 실제 북마크 생성, 조회, 삭제 로직을 담당
 * 
 * 주요 기능:
 * - 북마크 생성 로직 구현
 */
package com.codegenie.bookmark.service;

import com.codegenie.bookmark.entity.Bookmark;
import com.codegenie.bookmark.entity.SavedBookmark;
import com.codegenie.bookmark.repository.BookmarkRepository;
import com.codegenie.bookmark.repository.SavedBookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkServiceImpl implements BookmarkService {

    private final SavedBookmarkRepository savedBookmarkRepository;
    private final BookmarkRepository bookmarkRepository;

    @Override
    public void createBookmark(Long memberId, Long quizId) {
        // TODO: quizId로 CodingQuiz 엔티티를 조회하고, 연관된 Workbook의 topic을 가져오는 로직 필요
        // WorkbookRepository, CodingQuizRepository가 생성된 후 구현해야 함
        String topic = "임시 주제명"; // 임시로 토픽명을 하드코딩

        // 1. 사용자 ID와 토픽명으로 기존에 생성된 북마크 그룹이 있는지 확인
        Optional<SavedBookmark> optSavedBookmark = savedBookmarkRepository.findByMemberIdAndName(memberId, topic);

        SavedBookmark savedBookmark;
        if (optSavedBookmark.isEmpty()) {
            // 2-1. 그룹이 없으면 새로 생성
            savedBookmark = new SavedBookmark();
            savedBookmark.setMemberId(memberId);
            savedBookmark.setName(topic);
            savedBookmark = savedBookmarkRepository.save(savedBookmark);
        } else {
            // 2-2. 그룹이 있으면 기존 그룹 사용
            savedBookmark = optSavedBookmark.get();
        }

        // 3. 북마크 생성 및 저장
        Bookmark bookmark = new Bookmark();
        bookmark.setSavedBookmark(savedBookmark);
        bookmark.setQuizId(quizId);
        bookmarkRepository.save(bookmark);
    }
}
