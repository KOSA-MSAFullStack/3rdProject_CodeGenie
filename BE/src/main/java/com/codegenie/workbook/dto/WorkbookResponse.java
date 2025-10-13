package com.codegenie.workbook.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkbookResponse {
    private Long id;
    private String topic;
    private String language;
    private String level;
    private String style;
    private String requestDetail;
    private List<QuizResponse> quizzes;
}
