// BookmarkServiceImpl.java
// [서비스] 북마크 비즈니스 로직 구현체
/*
 * 설명:
 * - BookmarkService 인터페이스를 구현한 클래스
 * - is_saved 플래그를 이용한 북마크 로직을 담당
 *
 * 주요 기능:
 * - 북마크 토글, 저장된 퀴즈 조회 로직 구현
 */
package com.codegenie.bookmark.service;

import com.codegenie.bookmark.dto.BookmarkDetailDTO;
import com.codegenie.member.entity.MemberEntity;
import com.codegenie.member.repository.MemberRepository;
import com.codegenie.submission.dto.SubmissionResponseDto;
import com.codegenie.submission.entity.Submission;
import com.codegenie.submission.mapper.SubmissionMapper;
import com.codegenie.submission.repository.SubmissionRepository;
import com.codegenie.workbook.dto.QuizResponse;
import com.codegenie.workbook.entity.CodingQuiz;
import com.codegenie.workbook.entity.Workbook;
import com.codegenie.workbook.repository.CodingQuizRepository;
import com.codegenie.workbook.repository.WorkbookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkServiceImpl implements BookmarkService {

    private final CodingQuizRepository codingQuizRepository;
    private final WorkbookRepository workbookRepository;
    private final MemberRepository memberRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionMapper submissionMapper;

    @Override
    public void toggleBookmark(Integer quizId) {
        // 1. quizId로 CodingQuiz 조회
        CodingQuiz quiz = codingQuizRepository.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("해당 문제를 찾을 수 없습니다. ID: " + quizId));

        // 2. isSaved 상태를 반전
        quiz.setIsSaved(!quiz.getIsSaved());

        // 3. 변경된 상태를 저장
        codingQuizRepository.save(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkDetailDTO> getSavedQuizzes() {
        // 1. 현재 로그인한 사용자 정보 조회
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + email));

        // 2. 사용자의 모든 문제집 조회
        List<Workbook> workbooks = workbookRepository.findAllByMember(member);
        if (workbooks.isEmpty()) {
            return List.of(); // 문제집이 없으면 빈 리스트 반환
        }

        // 3. 저장된 퀴즈 목록 조회
        List<CodingQuiz> savedQuizzes = codingQuizRepository.findAllByWorkbookInAndIsSaved(workbooks, true);

        // 4. 각 퀴즈에 대한 마지막 제출 기록을 찾아 DTO로 변환
        return savedQuizzes.stream()
                .map(quiz -> {
                    // 퀴즈 정보를 QuizResponse DTO로 변환
                    QuizResponse quizResponse = QuizResponse.builder()
                            .id(quiz.getId())
                            .workbookId(quiz.getWorkbook().getId())
                            .quiz(quiz.getQuiz())
                            .explanation(quiz.getExplanation())
                            .concept(quiz.getConcept())
                            .isSaved(quiz.getIsSaved())
                            .build();

                    // 마지막 제출 기록 조회
                    Optional<Submission> lastSubmissionOpt = submissionRepository.findTopByMemberAndCodingQuizOrderBySubmittedAtDesc(member, quiz);

                    // 제출 기록이 있으면 SubmissionResponseDto로 변환
                    SubmissionResponseDto submissionResponse = lastSubmissionOpt
                            .map(submissionMapper::toDto)
                            .orElse(null);

                    // 최종 BookmarkDetailDTO로 빌드
                    return BookmarkDetailDTO.builder()
                            .quiz(quizResponse)
                            .lastSubmission(submissionResponse)
                            .build();
                })
                .collect(Collectors.toList());
    }
}