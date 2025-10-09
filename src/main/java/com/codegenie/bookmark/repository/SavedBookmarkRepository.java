// SavedBookmarkRepository.java
// [리포지토리] 북마크 그룹 데이터베이스 연동
/*
 * 설명:
 * - SavedBookmark 엔티티에 대한 데이터베이스 작업을 처리하는 JpaRepository
 * 
 * 주요 기능:
 * - 기본적인 CRUD (생성, 읽기, 수정, 삭제) 기능 자동 제공
 */

package com.codegenie.bookmark.repository;

import com.codegenie.bookmark.entity.SavedBookmark;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedBookmarkRepository extends JpaRepository<SavedBookmark, Long> {
    // JpaRepository를 상속받는 것만으로도 기본적인 DB 작업 (save, findById, findAll, delete 등)이 가능
    // 여기에 추가적으로 필요한 커스텀 쿼리 메소드를 선언할 수 있음 (예: 사용자 ID로 모든 북마크 그룹 찾기)
    Optional<SavedBookmark> findByMemberIdAndName(Long memberId, String name);
}
