// Bookmark.java
// [엔티티] 개별 북마크 항목 정보
/*
 * 설명:
 * - 사용자가 북마크한 개별 문제의 정보를 저장하는 엔티티
 * - 어느 북마크 그룹에 어떤 문제가 속해있는지를 정의
 * 
 * 주요 기능:
 * - 북마크 그룹과 문제 연결
 */

package com.codegenie.bookmark.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Bookmark {

    // 북마크 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id", nullable = false)
    private int quizId;
    
    @Column(name = "quiz", nullable = false)
    private String quiz;

    @Column(name = "explanation", nullable = true)
    private String explanation;

    @Column(name = "concept", nullable = true)
    private String concept;

    @Column(name = "is_saved", nullable = false)
    private boolean isSaved = false;
}