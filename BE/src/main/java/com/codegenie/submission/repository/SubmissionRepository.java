// SubmissionRepository.java
// 제출 정보 DB 연동
/*
 * 설명:
 * - Submission 엔티티에 대한 DB 작업 처리하는 Spring Data JPA 리포지토리
 *
 * 주요 기능:
 * - 제출 정보 저장, 조회, 삭제 등 기본적인 CRUD 제공
 * - 특정 조건(문제 ID, 사용자 ID, 채점 상태 등)에 따른 제출 정보 조회 기능 제공
 */

package com.codegenie.submission.repository;

import com.codegenie.submission.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

// * author: 김기성
public interface SubmissionRepository extends JpaRepository<Submission, Integer> {

    /**
     * 문제별 전체 제출수 조회
     * @param quizId 조회할 문제
     * @return 제출 횟수
     */
    long countByQuizId(Integer quizId);

    /**
     * 문제별 채점 상태 조회
     * @param quizId 조회할 문제
     * @param status 조회할 상태 (예: "Accepted")
     * @return 제출 횟수
     */
    long countByQuizIdAndStatus(Integer quizId, String status);

    /**
     * 문제별 가장 최근 제출 1건을 조회
     * @param memberId 현재 로그인한 사용자의 ID
     * @param quizId 조회할 문제
     * @return 가장 최신 제출 기록 (Optional)
     */
    Optional<Submission> findTopByMemberIdAndQuizIdOrderBySubmittedAtDesc(Integer memberId, Integer quizId);

    /**
     * 문제별 모든 제출 기록 최신순 조회
     * @param memberId 현재 로그인한 사용자의 ID
     * @param quizId 조회할 문제
     * @return 모든 제출 기록 (List)
     */
    List<Submission> findByMemberIdAndQuizIdOrderBySubmittedAtDesc(Integer memberId, Integer quizId);
}