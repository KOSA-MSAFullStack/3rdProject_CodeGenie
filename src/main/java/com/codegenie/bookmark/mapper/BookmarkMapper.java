// BookmarkMapper.java
// [매퍼] 북마크 엔티티-DTO 변환
/*
 * 설명:
 * - Bookmark 엔티티를 BookmarkDTO로 변환하는 유틸리티 클래스
 * 
 * 주요 기능:
 * - Entity를 DTO로 변환하여 프레젠테이션 계층에 안전하게 데이터 전달
 */
package com.codegenie.bookmark.mapper;

import com.codegenie.bookmark.dto.BookmarkDTO;
import com.codegenie.bookmark.entity.Bookmark;

public class BookmarkMapper {

    public static BookmarkDTO toDto(Bookmark bookmark) {
        if (bookmark == null) {
            return null;
        }

        BookmarkDTO dto = new BookmarkDTO();
        dto.setQuizId(bookmark.getQuizId());

        // TODO: CodingQuizRepository를 통해 quizId로 CodingQuiz 엔티티를 조회한 후,
        //       실제 문제 내용, 해설, 개념 데이터를 DTO에 설정해야 함.
        dto.setQuiz("임시 문제 내용");
        dto.setExplanation("임시 해설");
        dto.setConcept("임시 개념");

        return dto;
    }
}