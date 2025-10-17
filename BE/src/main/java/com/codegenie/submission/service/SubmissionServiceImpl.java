// src/main/java/com/codegenie/submission/service/SubmissionServiceImpl.java
package com.codegenie.submission.service;

import com.codegenie.member.entity.MemberEntity;
import com.codegenie.submission.dto.SubmissionDto;
import com.codegenie.submission.entity.SubmissionEntity;
import com.codegenie.submission.judge.LocalJudge;
import com.codegenie.submission.repository.SubmissionRepository;
import com.codegenie.workbook.entity.CodingQuiz;
import com.codegenie.workbook.repository.CodingQuizRepository;
import com.codegenie.workbooktestcase.entity.QuizTestcase;
import com.codegenie.workbooktestcase.repository.QuizTestcaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final CodingQuizRepository codingQuizRepository;
    private final QuizTestcaseRepository quizTestcaseRepository;
    private final LocalJudge localJudge;

    @Override
    @Transactional
    public SubmissionDto.SubmitResponse judge(MemberEntity member, SubmissionDto.SubmitRequest req) {

        if (req.getAnswer() == null || req.getAnswer().isBlank()) {
            // ⬇⬇ 언어 저장을 위해 judgeLang 계산 전에 기본값 사용
            return saveAndRespond(member, req, "Wrong Answer", null, "빈 코드 입니다.", null, null, "Java");
        }

        // 1) 문제 언어 정규화
        String judgeLang = "Java";
        try {
            Optional<CodingQuiz> cqOpt = codingQuizRepository.findById(req.getQuizId());
            if (cqOpt.isPresent() && cqOpt.get().getWorkbook() != null) {
                String raw = cqOpt.get().getWorkbook().getLanguage();
                String normalized = normalizeLanguage(raw);
                if (normalized != null) judgeLang = normalized;
            }
        } catch (Exception e) {
            log.warn("퀴즈 언어 조회/정규화 실패 quizId={}", req.getQuizId(), e);
        }

        // 2) 테스트케이스
        List<QuizTestcase> cases = quizTestcaseRepository.findByQuizIdOrderByIdAsc(req.getQuizId());
        if (cases == null || cases.isEmpty()) {
            return saveAndRespond(member, req, "Wrong Answer", null, "채점용 테스트케이스가 없습니다. 선택하신 언어를 확인해주세요.", null, null, judgeLang);
        }

        try {
            // 3) 채점
            LocalJudge.JudgeReport report = localJudge.judge(judgeLang, req.getAnswer(), cases);

            String status = report.getStatus();
            String stdout = ("Accepted".equals(status) ? "정답입니다!" : null);
            String stderr = null;

            if ("Compilation Error".equals(status)) stderr = nvl(report.getCompileLog(), "컴파일 에러");
            else if ("Time Limit Exceeded".equals(status)) stderr = "시간 제한을 초과했습니다.";
            else if ("Runtime Error".equals(status)) stderr = nvl(report.getRunLog(), "실행 중 에러가 발생했습니다.");
            else if ("Wrong Answer".equals(status)) stderr = nvl(report.getRunLog(), "틀렸습니다!");

            Double timeSeconds = report.getTotalTime();

            // ✅ judgeLang을 DB에 저장해서 NOT NULL 충족
            return saveAndRespond(member, req, status, stdout, stderr, timeSeconds, null, judgeLang);

        } catch (Exception ex) {
            log.error("Local judge failed. quizId={}, lang={}", req.getQuizId(), judgeLang, ex);
            return saveAndRespond(
                    member, req,
                    "Error",
                    null,
                    "채점 중 내부 오류가 발생했습니다: " + ex.getMessage(),
                    null,
                    null,
                    judgeLang // ✅ 예외여도 언어 저장
            );
        }
    }

    /* ============================ 저장 + 응답 공통 ============================ */

    // ⬇⬇ language 값을 인자로 추가
    private SubmissionDto.SubmitResponse saveAndRespond(
            MemberEntity member,
            SubmissionDto.SubmitRequest req,
            String status,
            String stdout,
            String stderr,
            Double timeSeconds,
            Integer memoryKb,
            String persistedLanguage // <-- 추가
    ) {
        SubmissionEntity saved = submissionRepository.save(
                SubmissionEntity.builder()
                        .quizId(req.getQuizId())
                        .memberId(member.getMember_id())
                        .answer(req.getAnswer())
                        .language(persistedLanguage)   // <-- 여기 null 금지
                        .status(status)
                        .stdout(stdout)
                        .stderr(stderr)
                        .runTime(timeSeconds)
                        .memory(memoryKb)
                        .submittedAt(LocalDateTime.now())
                        .build()
        );

        log.debug("submission saved: quizId={}, status={}", saved.getQuizId(), saved.getStatus());

        long totalSubmissions = submissionRepository.countByQuizId(req.getQuizId());
        long totalAccepted    = submissionRepository.countByQuizIdAndStatus(req.getQuizId(), "Accepted");

        return SubmissionDto.SubmitResponse.builder()
                .status(status)
                .stdout(stdout)
                .stderr(stderr)
                .time(timeSeconds)
                .memory(memoryKb)
                .submissions(totalSubmissions)
                .accepted(totalAccepted)
                .build();
    }

    /* ============================ 유틸 ============================ */

    private static String nvl(String s, String def) { return (s == null || s.isBlank()) ? def : s; }

    private static String normalizeLanguage(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase(Locale.ROOT);

        if (s.contains("자바")) return "Java";
        if (s.contains("파이썬")) return "Python";
        if (s.contains("씨") && (s.contains("플") || s.contains("++"))) return "C++";

        s = s.replaceAll("\\s+", "");
        if (s.startsWith("java") || s.equals("jav") || s.equals("jvaa")) return "Java";
        if (s.startsWith("py") || s.startsWith("python")) return "Python";
        if (s.contains("c++") || s.contains("cpp") || s.contains("cxx")) return "C++";

        if (s.equals("c") || s.equals("clang")) return "C++";
        return "Java";
    }
}
