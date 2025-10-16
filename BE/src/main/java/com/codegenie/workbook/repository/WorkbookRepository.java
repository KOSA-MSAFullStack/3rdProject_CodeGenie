package com.codegenie.workbook.repository;

import com.codegenie.member.entity.MemberEntity;
import com.codegenie.workbook.entity.Workbook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkbookRepository extends JpaRepository<Workbook, Integer> {
    /**
     * 특정 사용자가 생성한 모든 문제집 조회
     * @param member 조회할 사용자
     * @return 문제집 목록
     */
    List<Workbook> findAllByMember(MemberEntity member);
}