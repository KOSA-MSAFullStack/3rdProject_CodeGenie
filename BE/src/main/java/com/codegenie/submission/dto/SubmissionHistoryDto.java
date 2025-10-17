// src/main/java/com/codegenie/submission/dto/SubmissionHistoryDto.java
package com.codegenie.submission.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SubmissionHistoryDto {
    private Integer id;
    private String status;
    private String answer;
    private String stdout;
    private String stderr;
    private Double time;          // runTime
    private Integer memory;
    private LocalDateTime submittedAt;
}
