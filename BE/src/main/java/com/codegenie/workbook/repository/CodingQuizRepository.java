package com.codegenie.workbook.repository;

import com.codegenie.workbook.entity.CodingQuiz;
import com.codegenie.workbook.entity.Workbook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodingQuizRepository extends JpaRepository<CodingQuiz, Integer> {

    // ✅ coding_quizzes 테이블에 orderNo 컬럼이 없으므로 id 기준 정렬
    List<CodingQuiz> findByWorkbookIdOrderByIdAsc(Integer workbookId);

    /**
     * 주어진 문제집 목록에 포함되고, 저장 상태(isSaved)가 일치하는 모든 코딩테스트 문제 조회
     * @param workbooks 조회할 문제집 목록
     * @param isSaved 조회할 저장 상태 (true: 저장됨, false: 저장 안됨)
     * @return 코딩테스트 문제 목록
     */
    List<CodingQuiz> findAllByWorkbookInAndIsSaved(List<Workbook> workbooks, Boolean isSaved);
}
