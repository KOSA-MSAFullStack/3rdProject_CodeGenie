package com.codegenie.workbook.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateWorkbookRequest {
    private String language;         // 필수
    private String level;            // 초급/중급/고급
    private String style;            // 간단요약/깊이설명/예시중심
    private String requestDetail;    // 옵션
    private String topic;            // 학습 주제명(제목)
}
