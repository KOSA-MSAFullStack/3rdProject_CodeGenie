// SubmissionRepository.java
// 문제 제출 정보 'DB 접근&연동'
/*
 * 설명:
 * - Submission 엔티티에 대한 DB 작업을 처리하는 JpaRepository
 *
 * 주요 기능:
 * - 기본적인 CRUD (생성, 읽기, 수정, 삭제) 기능 자동 제공
 * - 사용자, 문제 기반의 제출 기록 조회
 */

package com.codegenie.submission.repository;

import com.codegenie.member.entity.MemberEntity;
import com.codegenie.submission.entity.Submission;
import com.codegenie.workbook.entity.CodingQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// * author: 김기성
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Integer> {
    // JpaRepository를 상속받는 것만으로도 기본적인 DB 작업 (save, findById, findAll, delete 등)이 가능

    /**
     * 특정 사용자가 특정 문제에 대해 제출한 기록 중 가장 최신 1개 조회
     * @param member 조회할 사용자
     * @param codingQuiz 조회할 문제
     * @return 가장 최신 제출 기록 (Optional)
     */
    Optional<Submission> findTopByMemberAndCodingQuizOrderBySubmittedAtDesc(MemberEntity member, CodingQuiz codingQuiz);
}