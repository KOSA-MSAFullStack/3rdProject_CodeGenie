package com.codegenie.bookmark.service;

import com.codegenie.bookmark.dto.BookmarkDetailDTO;
import com.codegenie.bookmark.dto.BookmarkedQuizViewDTO;
import com.codegenie.member.entity.MemberEntity;
import com.codegenie.member.repository.MemberRepository;
import com.codegenie.submission.entity.SubmissionEntity;
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

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkServiceImpl implements BookmarkService {

    private final CodingQuizRepository codingQuizRepository;
    private final WorkbookRepository workbookRepository;
    private final MemberRepository memberRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    public void toggleBookmark(Integer quizId) {
        CodingQuiz quiz = codingQuizRepository.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("해당 문제를 찾을 수 없습니다. ID: " + quizId));
        quiz.setIsSaved(!Boolean.TRUE.equals(quiz.getIsSaved()));
        codingQuizRepository.save(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkDetailDTO> getBookmarks() {
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
            SubmissionEntity last = submissionRepository
                    .findTopByMemberIdAndQuizIdOrderBySubmittedAtDesc(member.getMember_id(), q.getId())
                    .orElse(null);

            BookmarkDetailDTO.LastSubmission lastDto = (last == null) ? null :
                    BookmarkDetailDTO.LastSubmission.builder()
                            .status(last.getStatus())
                            .submittedAt(last.getSubmittedAt())
                            .answer(last.getAnswer())
                            .build();

            return BookmarkDetailDTO.builder()
                    .quiz(quizRes)
                    .lastSubmission(lastDto)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkedQuizViewDTO> getQuizzesForWorkbook(Integer workbookId) {
        return codingQuizRepository.findByWorkbookIdOrderByIdAsc(workbookId).stream()
                .map(this::toBookmarkedQuizViewDTO)
                .collect(Collectors.toList());
    }

    private BookmarkedQuizViewDTO toBookmarkedQuizViewDTO(CodingQuiz cq) {
        Sections s = splitSections(defaultIfBlank(cq.getQuiz(), ""));
        // 제출/정답 수는 여기선 0(필요 시 repository 이용해 채울 수 있음)
        return BookmarkedQuizViewDTO.builder()
                .id(cq.getId())
                .statement(s.statement)
                .input(s.input)
                .output(s.output)
                .sampleInput(s.sample)
                .explanation(defaultIfBlank(cq.getExplanation(), ""))
                .concept(defaultIfBlank(cq.getConcept(), ""))
                .isSaved(Boolean.TRUE.equals(cq.getIsSaved()))
                .spec(new BookmarkedQuizViewDTO.Spec(0L, 0L))
                .build();
    }

    private record Sections(String statement, String input, String output, String sample) { }

    private Sections splitSections(String raw) {
        String stmt = "", in = "", out = "", sample = "";
        var p = Pattern.compile("\\[(문제|입력|출력|예제 입력)]");
        var m = p.matcher(raw);
        List<Integer> idx = new ArrayList<>();
        List<String> lab = new ArrayList<>();
        while (m.find()) { idx.add(m.start()); lab.add(m.group(1)); }
        idx.add(raw.length());

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

    private static String defaultIfBlank(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getWorkbookTopicsWithBookmarks() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + email));

        List<Workbook> workbooks = workbookRepository.findAllByMember(member);
        if (workbooks.isEmpty()) return List.of();

        List<CodingQuiz> saved = codingQuizRepository.findAllByWorkbookInAndIsSaved(workbooks, true);

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
