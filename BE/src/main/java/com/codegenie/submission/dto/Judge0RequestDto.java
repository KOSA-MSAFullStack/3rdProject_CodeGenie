// Judge0RequestDto.java
// [DTO] Judge0 요청
/*
 * 설명:
 * - Judge0 API로 코드 채점을 요청할 때 사용하는 DTO
 * - 채점할 소스 코드와 언어 ID를 포함
 */
package com.codegenie.submission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

// * author: 김기성
@Getter
@AllArgsConstructor
public class Judge0RequestDto {
    @JsonProperty("source_code")    // 채점할 소스 코드
    private String sourceCode;

    @JsonProperty("language_id")    // Judge0에서 사용하는 언어 ID
    private int languageId;
}