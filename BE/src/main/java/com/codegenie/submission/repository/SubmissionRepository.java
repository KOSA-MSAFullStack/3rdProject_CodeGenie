package com.codegenie.submission.repository;

import com.codegenie.submission.entity.SubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Integer> {

    // 총 제출 수
    long countByQuizId(Integer quizId);

    // 정답 수
    long countByQuizIdAndStatus(Integer quizId, String status);
}
