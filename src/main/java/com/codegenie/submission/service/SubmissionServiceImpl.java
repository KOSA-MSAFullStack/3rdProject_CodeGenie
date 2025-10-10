// SubmissionServiceImpl.java
// [서비스] 문제 제출 비즈니스 로직 구현체
/*
 * 설명:
 * - SubmissionService 인터페이스를 구현한 클래스
 * - 실제 답안 제출 및 채점 요청 로직을 담당
 * 
 * 주요 기능:
 * - 답안 제출 로직 구현
 */
package com.codegenie.submission.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionServiceImpl implements SubmissionService {

    @Override
    public void submitAnswer(int memberId, int quizId, String answer) {
        
    }
}
