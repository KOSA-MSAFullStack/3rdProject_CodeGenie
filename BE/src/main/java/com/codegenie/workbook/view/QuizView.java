package com.codegenie.workbook.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizView {
    private Integer id;
    private String statement;
    private String input;
    private String output;

    // 프론트에서 sampleInput 키를 그대로 사용
    private String sampleInput;

    private String explanation;
    private String concept;

    private Spec spec;

    @Getter
    @AllArgsConstructor
    public static class Spec {
        private Long submissions;
        private Long accepted;
    }
}
