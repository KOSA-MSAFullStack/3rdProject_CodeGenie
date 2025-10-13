// Judge0ResponseDto.java
// [DTO] Judge0 응답
/*
 * 설명:
 * - Judge0 API로부터 받은 채점 결과를 담는 DTO
 * - Jackson 라이브러리를 사용하여 JSON 응답을 객체로 매핑
 */
package com.codegenie.submission.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

// * author: 김기성
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // Judge0 응답의 모든 필드를 매핑하지 않으므로, 모르는 필드는 무시하도록 설정
public class Judge0ResponseDto {

    // 표준 출력
    private String stdout;

    // 표준 에러
    private String stderr;

    // 실행 시간 (초)
    private Double time;

    // 사용 메모리 (KB)
    private Integer memory;

    // 컴파일 출력 (에러 포함)
    @JsonProperty("compile_output")
    private String compileOutput;

    // 채점 상태 객체
    private Status status;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        // 상태 ID (예: 3 = Accepted, 4 = Wrong Answer)
        private int id;

        // 상태 설명
        private String description;
    }
}