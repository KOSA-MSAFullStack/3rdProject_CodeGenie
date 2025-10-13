package com.codegenie.workbook.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "workbooks")
public class Workbook {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String language;            // Java, C++, ...
    private String level;               // 초급/중급/고급
    private String style;               // 간단요약/깊이설명/예시중심
    @Column(columnDefinition = "TEXT")
    private String requestDetail;       // 추가 요구사항
    private String topic;               // 학습 주제명 (제목)

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "workbook", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNo ASC")
    private List<CodingQuiz> quizzes = new ArrayList<>();
}
