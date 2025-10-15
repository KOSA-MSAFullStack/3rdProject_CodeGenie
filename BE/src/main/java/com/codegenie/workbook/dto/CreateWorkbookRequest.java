package com.codegenie.workbook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class CreateWorkbookRequest {

    private String language;
    private String level;
    private String style;

    // 프론트: request_detail, 백: requestDetail
    @JsonProperty("request_detail")
    private String requestDetail;

    private String topic;
}
