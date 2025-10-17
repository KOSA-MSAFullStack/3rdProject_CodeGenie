// src/main/java/com/codegenie/submission/entity/SubmissionEntity.java
package com.codegenie.submission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubmissionEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Integer id;

    @Column(name = "quiz_id", nullable = false)
    private Integer quizId;

    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Lob
    @Column(name = "answer", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String answer;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "status")
    private String status;

    @Lob @Column(name = "stdout")
    private String stdout;

    @Lob @Column(name = "stderr")
    private String stderr;

    @Column(name = "run_time")
    private Double runTime;

    @Column(name = "memory")
    private Integer memory;

    // ✅ INSERT 시점에 자동 세팅되도록 처리
    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    // ✅ 혹시나 null이 들어올 여지를 한 번 더 차단 (이중 안전장치)
    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }
}
