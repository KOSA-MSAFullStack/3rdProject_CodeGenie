// SubmissionServiceImpl.java
// 문제 제출 '비즈니스 로직 구현체'
/*
 * 설명:
 * - SubmissionService 인터페이스 구현, 코드 채점 로직 실제 처리
 * - LocalJudge 사용하여 코드를 실행, 그 결과를 DB에 저장, 사용자에게 응답
 *
 * 주요 기능:
 * - 제출된 코드의 유효성 검사
 * - 문제에 맞는 테스트케이스 조회
 * - LocalJudge를 통한 코드 채점 실행
 * - 채점 결과에 따른 상태(정답, 오답, 에러 등) 분류
 * - 채점 결과 Submission 엔티티로 변환하여 DB에 저장
 * - 최종 채점 결과를 DTO로 변환하여 컨트롤러에 반환
 */

package com.codegenie.submission.service;

import com.codegenie.member.entity.MemberEntity;
import com.codegenie.submission.dto.SubmissionDTO;
import com.codegenie.submission.entity.Submission;
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

// * author: 김기성
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
    public SubmissionDTO.SubmitResponse judge(MemberEntity member, SubmissionDTO.SubmitRequest req) {

        // 코드가 비어있는 경우, 오답 처리
        if (req.getAnswer() == null || req.getAnswer().isBlank()) {
            return saveAndRespond(member, req, "Wrong Answer", null, "빈 코드 입니다.", null, null, "Java");
        }

        // 1. 채점 언어 결정
        String judgeLang = "Java"; // 기본값
        try {
            // 문제(Quiz)에 설정된 언어 정보 조회하여 정규화
            Optional<CodingQuiz> cqOpt = codingQuizRepository.findById(req.getQuizId());
            if (cqOpt.isPresent() && cqOpt.get().getWorkbook() != null) {
                String raw = cqOpt.get().getWorkbook().getLanguage();
                String normalized = normalizeLanguage(raw);
                if (normalized != null) judgeLang = normalized;
            }
        } catch (Exception e) {
            log.warn("퀴즈 언어 조회/정규화 실패 quizId={}", req.getQuizId(), e);
        }

        // 2. 테스트케이스 조회
        List<QuizTestcase> cases = quizTestcaseRepository.findByQuizIdOrderByIdAsc(req.getQuizId());
        // 테스트케이스가 없는 경우, 오답 처리
        if (cases == null || cases.isEmpty()) {
            return saveAndRespond(member, req, "Wrong Answer", null, "채점용 테스트케이스가 없습니다. 선택하신 언어를 확인해주세요.", null, null, judgeLang);
        }

        try {
            // 3. 로컬 채점기(LocalJudge)를 통해 코드 채점 실행
            LocalJudge.JudgeReport report = localJudge.judge(judgeLang, req.getAnswer(), cases);

            String status = report.getStatus();
            String stdout = ("Accepted".equals(status) ? "정답입니다!" : null);
            String stderr = null;

            // 채점 결과 상태에 따라 표준 에러 메시지 설정
            if ("Compilation Error".equals(status)) stderr = nvl(report.getCompileLog(), "컴파일 에러");
            else if ("Time Limit Exceeded".equals(status)) stderr = "시간 제한을 초과했습니다.";
            else if ("Runtime Error".equals(status)) stderr = nvl(report.getRunLog(), "실행 중 에러가 발생했습니다.");
            else if ("Wrong Answer".equals(status)) stderr = nvl(report.getRunLog(), "틀렸습니다!");

            Double timeSeconds = report.getTotalTime();

            // 4. 채점 결과 DB에 저장, 사용자에게 응답
            return saveAndRespond(member, req, status, stdout, stderr, timeSeconds, null, judgeLang);

        } catch (Exception ex) {
            // 채점 중 예기치 않은 오류 발생 시, 내부 에러로 처리
            log.error("Local judge failed. quizId={}, lang={}", req.getQuizId(), judgeLang, ex);
            return saveAndRespond(
                    member, req,
                    "Error",
                    null,
                    "채점 중 내부 오류가 발생했습니다: " + ex.getMessage(),
                    null,
                    null,
                    judgeLang
            );
        }
    }

    /* ============================ 저장 + 응답 공통 ============================ */

    // 채점 결과 DB에 저장, 클라이언트에게 보낼 응답 DTO 생성하는 공통 메소드
    private SubmissionDTO.SubmitResponse saveAndRespond(
            MemberEntity member,             // 사용자 엔티티
            SubmissionDTO.SubmitRequest req, // 제출 요청 정보
            String status,                   // 채점 상태
            String stdout,                   // 표준 출력
            String stderr,                   // 표준 에러
            Double timeSeconds,              // 실행 시간
            Integer memoryKb,                // 메모리 사용량
            String persistedLanguage         // DB에 저장될 언어
    ) {
        // Submission 엔티티 생성 및 저장
        Submission saved = submissionRepository.save(
                Submission.builder()
                        .quizId(req.getQuizId())
                        .memberId(member.getMember_id())
                        .answer(req.getAnswer())
                        .language(persistedLanguage)
                        .status(status)
                        .stdout(stdout)
                        .stderr(stderr)
                        .runTime(timeSeconds)
                        .memory(memoryKb)
                        .submittedAt(LocalDateTime.now())
                        .build()
        );

        log.debug("submission saved: quizId={}, status={}", saved.getQuizId(), saved.getStatus());

        // 해당 문제의 총 제출 수와 정답 수 조회
        long totalSubmissions = submissionRepository.countByQuizId(req.getQuizId());
        long totalAccepted    = submissionRepository.countByQuizIdAndStatus(req.getQuizId(), "Accepted");

        // 최종 응답 DTO 생성 및 반환
        return SubmissionDTO.SubmitResponse.builder()
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

    // 문자열이 null or 비어있으면 기본값 반환 유틸리티
    private static String nvl(String s, String def) { return (s == null || s.isBlank()) ? def : s; }

    // 사용자가 입력한 언어 문자열을 표준화된 언어(Java, Python, C++)로 변환
    private static String normalizeLanguage(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase(Locale.ROOT);

        // 한글 키워드 포함 시
        if (s.contains("자바")) return "Java";
        if (s.contains("파이썬")) return "Python";
        if (s.contains("씨") && (s.contains("플") || s.contains("++ "))) return "C++";

        // 공백 제거 후 영어 키워드 확인
        s = s.replaceAll("\\s+", "");
        if (s.startsWith("java") || s.equals("jav") || s.equals("jvaa")) return "Java";
        if (s.startsWith("py") || s.startsWith("python")) return "Python";
        if (s.contains("c++") || s.contains("cpp") || s.contains("cxx")) return "C++";

        // 기타 유사 키워드 처리
        if (s.equals("c") || s.equals("clang")) return "C++";
        
        // 기본값으로 Java 반환
        return "Java";
    }
}