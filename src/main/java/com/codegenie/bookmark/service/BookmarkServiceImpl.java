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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkServiceImpl implements BookmarkService {

    @Override
    public void createBookmark(int memberId, int quizId) {
        
    }
}
