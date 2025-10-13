package com.codegenie.workbook.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizResponse {
    private Long id;
    private Integer orderNo;
    private String qname;
    private String statement;
    private String inputText;
    private String outputText;
    private String sampleInput;
    private String explanation;
    private String concept;
    private Long submissions;
    private Long accepted;
}
