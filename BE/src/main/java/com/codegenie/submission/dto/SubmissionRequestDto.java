// SubmissionRequestDto.java
// [DTO] 문제 제출 요청
/*
 * 설명:
 * - 클라이언트에서 서버로 코드 제출을 요청할 때 사용하는 DTO
 * - 사용자가 작성한 코드, 선택한 언어 등의 정보를 포함
 */
package com.codegenie.submission.dto;

import lombok.Getter;
import lombok.Setter;

// * author: 김기성
@Getter
@Setter
public class SubmissionRequestDto {
    // 문제 ID
    private Integer quizId;

    // 사용자가 작성한 소스 코드
    private String answer;

    // 사용 언어 (예: "Java", "Python")
    private String language;
}