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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// * author: 김기성
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
     * 현재 로그인한 사용자가 북마크한 모든 퀴즈 조회
     * @return 북마크된 퀴즈와 마지막 제출 기록이 담긴 DTO 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookmarkDetailDTO> getBookmarks() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + email));

        List<Workbook> userWorkbooks = workbookRepository.findAllByMember(member);
        if (userWorkbooks.isEmpty()) {
            return List.of();
        }

        List<CodingQuiz> savedQuizzes = codingQuizRepository.findAllByWorkbookInAndIsSaved(userWorkbooks, true);

        // N+1 문제를 피하기 위해, 관련된 문제집의 전체 퀴즈 목록을 미리 한 번에 가져옴
        Map<Integer, List<CodingQuiz>> allQuizzesByWorkbookMap = userWorkbooks.stream()
                .collect(Collectors.toMap(Workbook::getId, wb -> codingQuizRepository.findByWorkbookIdOrderByIdAsc(wb.getId())));

        // DTO로 변환
        return savedQuizzes.stream().map(quiz -> {
            long totalSubmissions = submissionRepository.countByCodingQuiz(quiz);
            long acceptedSubmissions = submissionRepository.countByCodingQuizAndStatus(quiz, "Accepted");
            QuizResponse.Spec spec = new QuizResponse.Spec(totalSubmissions, acceptedSubmissions);

            List<CodingQuiz> allQuizzesInWorkbook = allQuizzesByWorkbookMap.get(quiz.getWorkbook().getId());
            int quizNumber = -1; // 기본값
            if (allQuizzesInWorkbook != null) {
                quizNumber = allQuizzesInWorkbook.indexOf(quiz) + 1;
            }

            QuizResponse quizResponse = QuizResponse.builder()
                    .id(quiz.getId())
                    .workbookId(quiz.getWorkbook().getId())
                    .workbookTopic(quiz.getWorkbook().getTopic())
                    .quiz(quiz.getQuiz())
                    .explanation(quiz.getExplanation())
                    .concept(quiz.getConcept())
                    .isSaved(quiz.getIsSaved())
                    .quizNumber(quizNumber)
                    .spec(spec)
                    .build();

            Optional<Submission> lastSubmissionOpt = submissionRepository.findTopByMemberAndCodingQuizOrderBySubmittedAtDesc(member, quiz);
            SubmissionResponseDto submissionResponse = lastSubmissionOpt
                    .map(submissionMapper::toDto)
                    .orElse(null);

            return BookmarkDetailDTO.builder()
                    .quiz(quizResponse)
                    .lastSubmission(submissionResponse)
                    .build();
        }).collect(Collectors.toList());
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
                .id(cq.getId())             // 퀴즈 ID
                .statement(sec.statement)   // 문제 설명
                .input(sec.input)           // 입력 형식
                .output(sec.output)         // 출력 형식
                .sampleInput(sec.sample)    // 예제 입력
                .explanation(defaultIfBlank(cq.getExplanation(), ""))   // 해설
                .concept(defaultIfBlank(cq.getConcept(), ""))           // 관련 개념
                .isSaved(cq.getIsSaved())   // 북마크 여부
                .spec(new BookmarkedQuizViewDTO.Spec(0L, 0L))   // 퀴즈 스펙 (제출 수, 정답 수)
                .build();
    }

    // 퀴즈 내용을 섹션별로 분리하기 위한 레코드
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
     * 현재 로그인한 사용자가 북마크한 퀴즈가 있는 모든 문제집의 주제(topic) 목록 조회
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

        // 4. 북마크된 퀴즈가 있는 문제집의 고유한 topic을 ID 내림차순으로 추출
        return savedQuizzes.stream()
                .map(CodingQuiz::getWorkbook)
                .distinct()
                .sorted((wb1, wb2) -> wb2.getId().compareTo(wb1.getId())) // ID 내림차순 정렬
                .map(Workbook::getTopic)
                .filter(topic -> topic != null && !topic.isBlank())
                .collect(Collectors.toList());
    }
}
