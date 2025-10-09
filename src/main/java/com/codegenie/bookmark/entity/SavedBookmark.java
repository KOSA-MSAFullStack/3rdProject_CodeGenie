// SavedBookmark.java
// [엔티티] 북마크 그룹 정보
/*
 * 설명:
 * - 사용자가 저장한 북마크들을 그룹화하여 관리하는 엔티티
 * - '저장한 문제' 탭의 하위 탭 하나에 해당
 * 
 * 주요 기능:
 * - 북마크 그룹의 소유자 및 이름 관리
 */

package com.codegenie.bookmark.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class SavedBookmark {

    // 북마크 그룹 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long savedBookmarkId;

    // 북마크 그룹 이름 (문제집의 주제와 동일)
    @Column(nullable = false)
    private String name;

    // TODO: Member 엔티티 생성 후 @ManyToOne 관계로 수정 필요
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "member_id")
    // private Member member;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    // 이 그룹에 속한 북마크 목록
    @OneToMany(mappedBy = "savedBookmark", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bookmark> bookmarks = new ArrayList<>();
}
