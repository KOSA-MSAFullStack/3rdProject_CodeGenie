package com.codegenie.workbook.repository;

import com.codegenie.workbook.entity.CodingQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodingQuizRepository extends JpaRepository<CodingQuiz, Long> {

    // ✅ coding_quizzes 테이블에 orderNo 컬럼이 없으므로 id 기준 정렬
    List<CodingQuiz> findByWorkbookIdOrderByIdAsc(Long workbookId);
}
