package com.codegenie.workbook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WorkbookResponse {
    private Long id;

    @JsonProperty("member_id")
    private Long memberId;

    private String language;
    private String level;
    private String style;

    @JsonProperty("request_detail")
    private String requestDetail;

    @JsonProperty("is_upload")
    private Boolean isUpload;

    private String topic;

    // 상세 조회에서만 포함, 최근/생성 직후에는 null
    private List<QuizResponse> quizzes;
}
