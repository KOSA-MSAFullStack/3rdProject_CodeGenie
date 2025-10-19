// BookmarkServiceImpl.java
// 북마크 비즈니스 로직 구현체
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

import com.codegenie.bookmark.dto.BookmarkDTO;
import com.codegenie.bookmark.dto.BookmarkedQuizViewDTO;
import com.codegenie.member.entity.MemberEntity;
import com.codegenie.member.repository.MemberRepository;
import com.codegenie.submission.entity.Submission;
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
import java.util.*;
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

    /**
     * 북마크 상태 토글
     * @param quizId 퀴즈 ID
     */
    @Override
    public void toggleBookmark(Integer quizId) {
        // 1. quizId로 CodingQuiz 조회
        CodingQuiz quiz = codingQuizRepository.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("해당 문제를 찾을 수 없습니다. ID: " + quizId));
        quiz.setIsSaved(!Boolean.TRUE.equals(quiz.getIsSaved()));
        codingQuizRepository.save(quiz);
    }

    /**
     * 현재 로그인한 사용자가 북마크한 모든 퀴즈 조회
     * @return 북마크된 퀴즈와 마지막 제출 기록이 담긴 DTO 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookmarkDTO> getBookmarks() {
        // 현재 로그인 사용자
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + email));

        // 내 문제집만 대상으로 저장된 문제
        List<Workbook> myWorkbooks = workbookRepository.findAllByMember(member);
        if (myWorkbooks.isEmpty()) return List.of();

        List<CodingQuiz> savedQuizzes =
                codingQuizRepository.findAllByWorkbookInAndIsSaved(myWorkbooks, true);

        // 각 문제집의 전체 퀴즈 목록 미리 로드 → quizNumber 계산용
        Map<Integer, List<CodingQuiz>> quizzesByWb = myWorkbooks.stream()
                .collect(Collectors.toMap(
                        Workbook::getId,
                        wb -> codingQuizRepository.findByWorkbookIdOrderByIdAsc(wb.getId())
                ));

        return savedQuizzes.stream().map(q -> {
            long submissions = submissionRepository.countByQuizId(q.getId());
            long accepted = submissionRepository.countByQuizIdAndStatus(q.getId(), "Accepted");

            // 번호 계산
            int quizNo = -1;
            List<CodingQuiz> allInWb = quizzesByWb.get(q.getWorkbook().getId());
            if (allInWb != null) {
                quizNo = allInWb.indexOf(q) + 1;
            }

            QuizResponse.Spec spec = new QuizResponse.Spec(submissions, accepted);
            QuizResponse quizRes = QuizResponse.builder()
                    .id(q.getId())
                    .workbookId(q.getWorkbook().getId())
                    .workbookTopic(q.getWorkbook().getTopic())
                    .quiz(defaultIfBlank(q.getQuiz(), ""))
                    .explanation(defaultIfBlank(q.getExplanation(), ""))
                    .concept(defaultIfBlank(q.getConcept(), ""))
                    .isSaved(Boolean.TRUE.equals(q.getIsSaved()))
                    .quizNumber(quizNo)
                    .spec(spec)
                    .build();

            // 유저의 마지막 제출 1건
            Submission last = submissionRepository
                    .findTopByMemberIdAndQuizIdOrderBySubmittedAtDesc(member.getMember_id(), q.getId())
                    .orElse(null);

            BookmarkDTO.LastSubmission lastDto = (last == null) ? null :
                    BookmarkDTO.LastSubmission.builder()
                            .status(last.getStatus())
                            .submittedAt(last.getSubmittedAt())
                            .answer(last.getAnswer())
                            .build();

            return BookmarkDTO.builder()
                    .quiz(quizRes)
                    .lastSubmission(lastDto)
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
        return codingQuizRepository.findByWorkbookIdOrderByIdAsc(workbookId).stream()
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
        Sections s = splitSections(defaultIfBlank(cq.getQuiz(), ""));

        // 제출/정답 수는 여기선 0(필요 시 repository 이용해 채울 수 있음)
        return BookmarkedQuizViewDTO.builder()
                .id(cq.getId())         // 퀴즈 ID
                .statement(s.statement) // 문제 설명
                .input(s.input)         // 입력 형식
                .output(s.output)       // 출력 형식
                .sampleInput(s.sample)  // 예제 입력
                .explanation(defaultIfBlank(cq.getExplanation(), ""))           // 해설
                .concept(defaultIfBlank(cq.getConcept(), ""))                   // 개념
                .isSaved(Boolean.TRUE.equals(cq.getIsSaved()))                      // 북마크 여부
                .spec(new BookmarkedQuizViewDTO.Spec(0L, 0L))  // 퀴즈 스펙 (제출 수, 정답 수)
                .build();
    }
 
    // 퀴즈 내용 섹션별로 분리하기 위한 레코드
    private record Sections(String statement, String input, String output, String sample) { }

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
        idx.add(raw.length());

        // 섹션별 내용 추출
        for (int i = 0; i < lab.size(); i++) {
            String label = lab.get(i);
            int from = raw.indexOf(']', idx.get(i)) + 1;
            int to = idx.get(i + 1);
            String body = raw.substring(Math.max(from,0), Math.max(to, from)).trim();
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
        if (workbooks.isEmpty()) return List.of();

        // 3. 저장된 퀴즈 목록 조회
        List<CodingQuiz> saved = codingQuizRepository.findAllByWorkbookInAndIsSaved(workbooks, true);

        // 4. 북마크된 퀴즈가 있는 문제집의 고유한 topic을 ID 내림차순으로 추출
        return saved.stream()
                .map(CodingQuiz::getWorkbook)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Workbook::getId, Workbook::getTopic, (a, b) -> a, TreeMap::new
                ))
                .descendingMap()
                .values()
                .stream()
                .filter(t -> t != null && !t.isBlank())
                .toList();
    }
}
