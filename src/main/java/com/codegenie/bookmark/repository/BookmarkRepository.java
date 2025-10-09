// BookmarkRepository.java
// [리포지토리] 개별 북마크 데이터베이스 연동
/*
 * 설명:
 * - Bookmark 엔티티에 대한 데이터베이스 작업을 처리하는 JpaRepository
 * 
 * 주요 기능:
 * - 기본적인 CRUD (생성, 읽기, 수정, 삭제) 기능 자동 제공
 */

package com.codegenie.bookmark.repository;

import com.codegenie.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    // JpaRepository를 상속받는 것만으로도 기본적인 DB 작업 (save, findById, findAll, delete 등)이 가능
}
