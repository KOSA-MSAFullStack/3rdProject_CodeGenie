// Judge0RequestDto.java
// [DTO] Judge0 요청
/*
 * 설명:
 * - Judge0 API로 코드 채점을 요청할 때 사용하는 DTO
 * - 채점할 소스 코드와 언어 ID를 포함
 */

package com.codegenie.submission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

// * author: 김기성
@Getter
public class Judge0RequestDto {
    @JsonProperty("source_code")
    private final String sourceCode;

    @JsonProperty("language_id")
    private final int languageId;

    public Judge0RequestDto(String sourceCode, int languageId) {
        this.sourceCode = sourceCode;
        this.languageId = languageId;
    }
}