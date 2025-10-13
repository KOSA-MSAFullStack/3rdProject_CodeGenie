package com.codegenie.workbook.repository;

import com.codegenie.workbook.entity.CodingQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodingQuizRepository extends JpaRepository<CodingQuiz, Long> {
    List<CodingQuiz> findByWorkbookIdOrderByOrderNoAsc(Long workbookId);
}
