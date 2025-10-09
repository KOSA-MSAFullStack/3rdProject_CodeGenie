// BookmarkDTO.java
// [DTO] 개별 북마크 정보
/*
 * 설명:
 * - 북마크된 개별 문제의 정보를 화면에 전달하기 위한 DTO
 * - Entity를 직접 노출하지 않고 필요한 데이터만 가공해서 전달
 * 
 * 주요 기능:
 * - 문제 ID, 제목 등 화면에 필요한 문제 정보 제공
 */
package com.codegenie.bookmark.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookmarkDTO {
    private Long quizId;
    private String quiz; // 문제 내용
    private String explanation; // 해설
    private String concept; // 개념
}
