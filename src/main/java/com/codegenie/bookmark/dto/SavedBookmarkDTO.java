// SavedBookmarkDTO.java
// [DTO] 북마크 그룹 정보
/*
 * 설명:
 * - 북마크 그룹의 정보를 화면에 전달하기 위한 DTO
 * - 그룹 이름과 그룹에 속한 북마크 목록을 포함
 * 
 * 주요 기능:
 * - '저장한 문제' 탭의 그룹별 데이터를 표현
 */
package com.codegenie.bookmark.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SavedBookmarkDTO {
    private String name;
    private List<BookmarkDTO> bookmarks;
}