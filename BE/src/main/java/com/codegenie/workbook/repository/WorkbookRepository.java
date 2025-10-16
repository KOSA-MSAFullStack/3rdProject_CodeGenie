package com.codegenie.workbook.repository;

import com.codegenie.member.entity.MemberEntity;
import com.codegenie.workbook.entity.Workbook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkbookRepository extends JpaRepository<Workbook, Integer> {
    /**
     * 특정 사용자가 생성한 모든 문제집 조회
     */
    List<Workbook> findAllByMember(MemberEntity member);

    /** ✅ 추가: 내 것만 최신순 */
    List<Workbook> findByMemberOrderByIdDesc(MemberEntity member);

    /** ✅ 추가: 단건 조회 시에도 소유자까지 검증 */
    Optional<Workbook> findByIdAndMember(Integer id, MemberEntity member);
}
