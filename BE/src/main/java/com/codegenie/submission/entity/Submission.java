// Submission.java
// [엔티티] 문제 제출 정보
/*
 * 설명:
 * - 사용자가 제출한 코드, 언어, 실행 결과 등을 저장하는 엔티티
 * - Judge0 API를 통해 채점된 결과를 관리
 *
 * 주요 기능:
 * - 제출된 코드 및 채점 결과 관리
 */
package com.codegenie.submission.entity;

import com.codegenie.member.entity.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// * author: 김기성
@Entity
@Getter
@Setter
@Table(name = "submissions")
public class Submission {

    // 제출 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Integer submissionId;

    // 사용자 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    // 문제 ID (FK)
    @Column(name = "quiz_id", nullable = false)
    private Integer quizId;

    // 제출된 소스 코드
    @Column(name = "answer", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String answer;

    // 사용 언어
    @Column(nullable = false)
    private String language;

    // 채점 상태
    private String status;

    // 표준 출력
    @Column(columnDefinition = "TEXT")
    private String stdout;

    // 표준 에러
    @Column(columnDefinition = "TEXT")
    private String stderr;

    // 실행 시간 (초)
    @Column(name = "run_time")
    private Double runTime;

    // 사용 메모리 (KB)
    private Integer memory;

    // 제출 시간
    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;
}