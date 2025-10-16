// BookmarkServiceImpl.java
// [서비스] 북마크 비즈니스 로직 구현체
/*
 * 설명:
 * - BookmarkService 인터페이스 구현한 클래스
 * - is_saved 플래그를 이용한 북마크 로직 담당
 *
 * 주요 기능:
 * - 북마크 토글, 저장된 퀴즈 조회 로직 구현
 * - 문제집 내 퀴즈 목록 조회 (북마크 상태 포함)
 */
package com.codegenie.bookmark.service;

import com.codegenie.bookmark.dto.BookmarkDetailDTO;
import com.codegenie.bookmark.dto.BookmarkedQuizViewDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Set; // New import
import java.util.HashSet; // New import

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkServiceImpl implements BookmarkService {

    private final CodingQuizRepository codingQuizRepository;
    private final WorkbookRepository workbookRepository;
    private final MemberRepository memberRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionMapper submissionMapper;

    /**
     * 북마크 상태 토글
     * @param quizId 퀴즈 ID
     */
    @Override
    public void toggleBookmark(Integer quizId) {
        // 1. quizId로 CodingQuiz 조회
        // if (조건) throw 예외: 해당 문제를 찾을 수 없음
        CodingQuiz quiz = codingQuizRepository.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("해당 문제를 찾을 수 없습니다. ID: " + quizId));

        // 2. isSaved 상태 반전
        quiz.setIsSaved(!quiz.getIsSaved());

        // 3. 변경된 상태 저장
        codingQuizRepository.save(quiz);
    }

    /**
     * 현재 로그인한 사용자가 북마크한 모든 퀴즈를 조회
     * @return 북마크된 퀴즈와 마지막 제출 기록이 담긴 DTO 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookmarkDetailDTO> getSavedQuizzes() { // Reverted return type
        // 1. 현재 로그인한 사용자 정보 조회
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // if (조건) throw 예외: 사용자를 찾을 수 없음
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + email));

        // 2. 사용자의 모든 문제집 조회
        List<Workbook> workbooks = workbookRepository.findAllByMember(member);
        // if (조건) return 빈 리스트: 문제집이 없으면 빈 리스트 반환
        if (workbooks.isEmpty()) {
            return List.of();
        }

        // 3. 저장된 퀴즈 목록 조회
        List<CodingQuiz> savedQuizzes = codingQuizRepository.findAllByWorkbookInAndIsSaved(workbooks, true);

        // 4. 각 퀴즈에 대한 마지막 제출 기록을 찾아 DTO로 변환 (그룹화 로직 제거)
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

    /**
     * 특정 문제집에 속한 모든 퀴즈를 북마크 상태와 함께 조회
     * @param workbookId 문제집 ID
     * @return 북마크 상태가 포함된 퀴즈 뷰 DTO 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookmarkedQuizViewDTO> getQuizzesForWorkbook(Integer workbookId) {
        // workbookId로 퀴즈 목록 조회 후 BookmarkedQuizViewDTO로 매핑
        return codingQuizRepository.findByWorkbookIdOrderByIdAsc(workbookId)
                .stream()
                .map(this::toBookmarkedQuizViewDTO)
                .collect(Collectors.toList());
    }

    /**
     * CodingQuiz 엔티티를 BookmarkedQuizViewDTO로 변환
     * @param cq CodingQuiz 엔티티
     * @return BookmarkedQuizViewDTO
     */
    private BookmarkedQuizViewDTO toBookmarkedQuizViewDTO(CodingQuiz cq) {
        // 퀴즈 내용을 섹션별로 분리
        Sections sec = splitSections(defaultIfBlank(cq.getQuiz(), ""));
        return BookmarkedQuizViewDTO.builder()
                .id(cq.getId()) // 퀴즈 ID
                .statement(sec.statement) // 문제 설명
                .input(sec.input) // 입력 형식
                .output(sec.output) // 출력 형식
                .sampleInput(sec.sample) // 예제 입력
                .explanation(defaultIfBlank(cq.getExplanation(), "")) // 해설
                .concept(defaultIfBlank(cq.getConcept(), "")) // 관련 개념
                .isSaved(cq.getIsSaved()) // 북마크 여부
                .spec(new BookmarkedQuizViewDTO.Spec(0L, 0L)) // 퀴즈 스펙 (제출 수, 정답 수)
                .build();
    }

    // Helper methods copied from WorkbookServiceImpl to maintain package isolation
    /**
     * 퀴즈 내용을 섹션별로 분리하기 위한 레코드
     */
    private record Sections(String statement, String input, String output, String sample) {}
    /**
     * 퀴즈 내용을 문제, 입력, 출력, 예제 입력 섹션으로 분리
     * @param raw 원본 퀴즈 내용
     * @return 분리된 섹션
     */
    private Sections splitSections(String raw) {
        String stmt = "", in = "", out = "", sample = "";
        // 정규식 패턴: [문제], [입력], [출력], [예제 입력]
        var p = Pattern.compile("\\[(문제|입력|출력|예제 입력)]");
        var m = p.matcher(raw);
        List<Integer> idx = new ArrayList<>();
        List<String> lab = new ArrayList<>();
        // 각 섹션의 시작 인덱스와 레이블 추출
        while (m.find()) { idx.add(m.start()); lab.add(m.group(1)); } 
        idx.add(raw.length()); // 마지막 섹션의 끝 인덱스

        // 섹션별 내용 추출
        for (int i = 0; i < lab.size(); i++) {
            String label = lab.get(i);
            int from = raw.indexOf(']', idx.get(i)) + 1;
            int to = idx.get(i + 1);
            String body = raw.substring(Math.max(from, 0), Math.max(to, from)).trim();
            switch (label) {
                case "문제" -> stmt = body;
                case "입력" -> in = body;
                case "출력" -> out = body;
                case "예제 입력" -> sample = body;
            }
        }
        return new Sections(stmt, in, out, sample);
    }

    /**
     * 문자열이 null이거나 비어있으면 기본값 반환
     * @param s 원본 문자열
     * @param def 기본값
     * @return 처리된 문자열
     */
    private static String defaultIfBlank(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    /**
     * 현재 로그인한 사용자가 북마크한 퀴즈가 있는 모든 문제집의 주제(topic) 목록을 조회
     * @return 북마크된 퀴즈가 있는 문제집 주제 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> getWorkbookTopicsWithBookmarks() {
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

        // 4. 북마크된 퀴즈가 있는 문제집의 고유한 topic 추출
        Set<String> uniqueTopics = new HashSet<>();
        for (CodingQuiz quiz : savedQuizzes) {
            if (quiz.getWorkbook() != null && quiz.getWorkbook().getTopic() != null && !quiz.getWorkbook().getTopic().isBlank()) {
                uniqueTopics.add(quiz.getWorkbook().getTopic());
            }
        }

        return new ArrayList<>(uniqueTopics);
    }
}