// src/main/java/com/codegenie/submission/repository/SubmissionRepository.java
package com.codegenie.submission.repository;

import com.codegenie.submission.entity.SubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;      // ✅ 추가
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, Integer> {

    // 문제별 전체 제출 수
    long countByQuizId(Integer quizId);

    // 문제별 정답 수
    long countByQuizIdAndStatus(Integer quizId, String status);

    // 조회: 현재 사용자(memberId)의 특정 퀴즈(quizId) 제출 내역, 최신순
    List<SubmissionEntity> findByMemberIdAndQuizIdOrderBySubmittedAtDesc(Integer memberId, Integer quizId);

    // 특정 사용자·문제에 대한 가장 최근 제출 1건
    Optional<SubmissionEntity> findTopByMemberIdAndQuizIdOrderBySubmittedAtDesc(Integer memberId, Integer quizId);

    // (선택) primitive 사용 시 오버로드가 편하면 아래도 추가해도 됩니다.
    // Optional<SubmissionEntity> findTopByMemberIdAndQuizIdOrderBySubmittedAtDesc(int memberId, int quizId);
}
