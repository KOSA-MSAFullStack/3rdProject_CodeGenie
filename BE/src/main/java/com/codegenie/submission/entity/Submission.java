// Submission.java
// 문제 제출 정보, 'DB 테이블과 직접 매핑 객체'
/*
 * 설명:
 * - 사용자가 제출한 코드, 언어, 실행 결과 등을 저장하는 엔티티
 * - Judge0 API를 통해 채점된 결과를 관리
 *
 * 주요 기능:
 * - 제출된 코드 및 채점 결과 관리
 */

package com.codegenie.submission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

// * author: 김기성
@Entity
@Table(name = "submissions")
@Getter
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Submission {

    // 제출 ID (PK)
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Integer id;

    // 문제 ID (FK)
    @Column(name = "quiz_id", nullable = false)
    private Integer quizId;

    // 사용자 ID (FK)
    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    // 제출된 소스 코드
    @Lob
    @Column(name = "answer", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String answer;

    // 사용 언어
    @Column(name = "language", nullable = false)
    private String language;

    // 채점 상태
    @Column(name = "status")
    private String status;

    // 표준 출력
    @Lob 
    @Column(name = "stdout")
    private String stdout;

    // 표준 에러
    @Lob 
    @Column(name = "stderr")
    private String stderr;

    // 실행 시간 (초)
    @Column(name = "run_time")
    private Double runTime;

    // 사용 메모리 (KB)
    @Column(name = "memory")
    private Integer memory;

    // 제출 시간
    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false, nullable = false)
    private LocalDateTime submittedAt;

    // 제출 시간 null 방지 (이중 안전장치)
    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }
}
