package com.codegenie.workbooktestcase.repository;

import com.codegenie.workbooktestcase.entity.QuizTestcase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizTestcaseRepository extends JpaRepository<QuizTestcase, Integer> {
    List<QuizTestcase> findByQuizIdOrderByIdAsc(Integer quizId);
}
