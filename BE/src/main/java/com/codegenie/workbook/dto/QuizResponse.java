package com.codegenie.workbook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizResponse {
    private Integer id;

    @JsonProperty("workbook_id")
    private Integer workbookId;

    private String workbookTopic; // 문제집 주제 추가

    private String quiz;
    private String explanation;
    private String concept;

    @JsonProperty("is_saved")
    private boolean isSaved;

    private int problemNumber; // 문제 번호 추가

    private Spec spec;

    public record Spec(long submissions, long accepted) {}
}
