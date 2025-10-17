// src/main/java/com/codegenie/workbooktestcase/entity/QuizTestcase.java
package com.codegenie.workbooktestcase.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "quiz_testcases")
@Getter
@Setter
public class QuizTestcase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "quiz_id", nullable = false)
    private Integer quizId;

    @Lob
    @Column(name = "input_text", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String inputText;

    @Lob
    @Column(name = "expected_out", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String expectedOut;   // ✅ WorkbookServiceImpl과 일치

    @Column(name = "is_sample", nullable = false)
    private Boolean isSample = false;

    @Column(name = "weight", nullable = false)
    private Integer weight = 1;

    /* ====== 헷갈릴 수 있는 부분 보강용 메서드 ====== */
    // IDE가 메서드 시그니처를 제대로 인식하지 못할 때를 대비
    public Boolean getIsSample() {
        return isSample;
    }

    public void setIsSample(Boolean isSample) {
        this.isSample = isSample;
    }

    public String getExpectedOut() {
        return expectedOut;
    }

    public void setExpectedOut(String expectedOut) {
        this.expectedOut = expectedOut;
    }
}
