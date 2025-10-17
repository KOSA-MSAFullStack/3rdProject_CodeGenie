package com.codegenie.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BookmarkedQuizViewDTO {
    private Integer id;
    private String statement;
    private String input;
    private String output;
    private String sampleInput;
    private String explanation;
    private String concept;
    private Boolean isSaved;
    private Spec spec;

    @Getter
    @AllArgsConstructor
    public static class Spec {
        private Long submissions;
        private Long accepted;
    }
}
