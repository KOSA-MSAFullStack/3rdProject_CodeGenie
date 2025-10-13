package com.codegenie.workbook.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "coding_quizzes")
public class CodingQuiz {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workbook_id")
    private Workbook workbook;

    private Integer orderNo;            // 1..10
    private String qname;               // 문제 1..10

    @Column(columnDefinition = "TEXT")
    private String statement;
    @Column(columnDefinition = "TEXT")
    private String inputText;
    @Column(columnDefinition = "TEXT")
    private String outputText;
    @Column(columnDefinition = "TEXT")
    private String sampleInput;

    @Column(columnDefinition = "TEXT")
    private String explanation;         // 해설
    @Column(columnDefinition = "TEXT")
    private String concept;             // 개념 요약

    private Long submissions;           // 제출 수
    private Long accepted;              // 정답 수
}
