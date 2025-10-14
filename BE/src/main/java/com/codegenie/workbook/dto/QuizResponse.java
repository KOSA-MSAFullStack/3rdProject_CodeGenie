package com.codegenie.workbook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizResponse {
    private Long id;

    @JsonProperty("workbook_id")
    private Long workbookId;

    private String quiz;
    private String explanation;
    private String concept;

    @JsonProperty("is_saved")
    private boolean isSaved;
}
