package com.codegenie.workbook.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "coding_quizzes")
public class CodingQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workbook_id", nullable = false)
    private Workbook workbook;

    @Lob
    @Column(name = "quiz", nullable = false, columnDefinition = "TEXT")
    private String quiz;

    @Lob
    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Lob
    @Column(name = "concept", columnDefinition = "TEXT")
    private String concept;

    @Builder.Default
    @Column(name = "is_saved", nullable = false)
    private Boolean isSaved = Boolean.FALSE;
}
