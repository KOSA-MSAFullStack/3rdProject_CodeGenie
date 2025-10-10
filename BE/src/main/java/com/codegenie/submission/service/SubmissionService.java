// SubmissionService.java
// [서비스] 문제 제출 비즈니스 로직 인터페이스
/*
 * 설명:
 * - 문제 제출 관련 비즈니스 로직을 정의하는 인터페이스
 * 
 * 주요 기능:
 * - 답안 제출 및 채점 요청 기능 명세
 */
package com.codegenie.submission.service;

public interface SubmissionService {

    /**
     * 답안 제출하고 채점 요청
     * @param memberId 현재 로그인한 사용자의 ID
     * @param quizId 제출할 문제의 ID
     * @param answer 사용자가 작성한 답안 코드
     */
    void submitAnswer(int memberId, int quizId, String answer);
}
