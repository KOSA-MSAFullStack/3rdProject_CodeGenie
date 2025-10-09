// Submission.java
// [엔티티] 문제 제출 정보
/*
 * 설명:
 * - 사용자가 제출한 답안과 채점 결과를 저장하는 엔티티
 * 
 * 주요 기능:
 * - 제출된 코드, 채점 결과(점수, 성공 여부) 관리
 */

package com.codegenie.submission.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Submission {

    // 제출 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long submissionId;

    // 제출된 소스 코드
    @Lob
    @Column(nullable = false)
    private String answer;

    // 채점 점수
    private Integer score;

    // 채점 결과 (e.g., "SUCCESS", "FAIL", "ERROR")
    private String result;

    // TODO: Member 엔티티 생성 후 @ManyToOne 관계로 수정 필요
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "member_id")
    // private Member member;
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    // TODO: CodingQuiz 엔티티 생성 후 @ManyToOne 관계로 수정 필요
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "quiz_id")
    // private CodingQuiz quiz;
    @Column(name = "quiz_id", nullable = false)
    private Long quizId;
}