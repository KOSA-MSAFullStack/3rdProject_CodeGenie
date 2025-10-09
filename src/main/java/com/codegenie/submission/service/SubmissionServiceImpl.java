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

import com.codegenie.submission.entity.Submission;
import com.codegenie.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;

    @Override
    public void submitAnswer(Long memberId, Long quizId, String answer) {
        // 1. 제출 정보로 Submission 엔티티 생성
        Submission submission = new Submission();
        submission.setMemberId(memberId);
        submission.setQuizId(quizId);
        submission.setAnswer(answer);

        // 2. DB에 제출 정보 저장
        submissionRepository.save(submission);

        // TODO: Spring AI를 이용한 비동기 채점 로직 호출 부분
        // grade(submission);
    }
}
