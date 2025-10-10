// Submission.java
// [엔티티] 문제 제출 정보
/*
 * 설명:
 * - 사용자가 제출한 답안을 저장하는 엔티티
 * 
 * 주요 기능:
 * - 제출된 코드 관리
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
    private int submissionId;

    // 제출된 소스 코드
    @Column(name = "answer", nullable = false)
    private String answer;
}