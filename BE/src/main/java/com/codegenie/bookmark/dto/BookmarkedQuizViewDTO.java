// BookmarkedQuizViewDTO.java
// [DTO] 북마크 상태가 포함된 퀴즈 뷰
/*
 * 설명:
 * - 기존 QuizView와 유사하지만, 북마크 상태(isSaved)를 반드시 포함하는 DTO
 * - bookmark 패키지 내에서 사용하여 다른 패키지와의 의존성을 최소화
 *
 * 주요 기능:
 * - 문제집 상세 보기에서 퀴즈 목록 반환할 때 사용
 */
package com.codegenie.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// * author: 김기성
@Getter
@Builder
@AllArgsConstructor
public class BookmarkedQuizViewDTO {
    private Integer id;         // 퀴즈 ID
    private String statement;   // 문제 설명
    private String input;       // 입력 형식
    private String output;      // 출력 형식
    private String sampleInput; // 예제 입력
    private String explanation; // 해설
    private String concept;     // 관련 개념
    private Boolean isSaved;    // 북마크 여부
    private Spec spec;          // 퀴즈 스펙 (제출 수, 정답 수)

    @Getter
    @AllArgsConstructor
    public static class Spec {
        private Long submissions;   // 총 제출 수
        private Long accepted;      // 정답 수
    }
}