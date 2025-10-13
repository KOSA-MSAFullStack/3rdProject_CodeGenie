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

import com.codegenie.member.entity.MemberEntity;
import com.codegenie.member.repository.MemberRepository;
import com.codegenie.submission.dto.Judge0RequestDto;
import com.codegenie.submission.dto.Judge0ResponseDto;
import com.codegenie.submission.dto.SubmissionRequestDto;
import com.codegenie.submission.dto.SubmissionResponseDto;
import com.codegenie.submission.entity.Submission;
import com.codegenie.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.NoSuchElementException;

// * author: 김기성
@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final MemberRepository memberRepository;
    private final RestTemplate restTemplate;

    @Value("${judge0.api.url}")
    private String judge0ApiUrl;

    // 지원하는 언어와 Judge0 ID 매핑
    private static final Map<String, Integer> LANGUAGE_MAP = Map.of(
            "Java", 62,
            "Python", 71,
            "C++", 54);

    @Override
    public SubmissionResponseDto submitAnswer(SubmissionRequestDto requestDto, Integer memberId) {
        // 1. 사용자 조회
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("해당 사용자를 찾을 수 없습니다. ID: " + memberId));

        // 2. 언어 ID 조회
        int languageId = getLanguageId(requestDto.getLanguage());

        // 3. Judge0 API 요청 생성
        Judge0RequestDto judge0Request = new Judge0RequestDto(requestDto.getAnswer(), languageId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Judge0RequestDto> requestEntity = new HttpEntity<>(judge0Request, headers);

        // 4. Judge0 API 호출
        Judge0ResponseDto judge0Response;
        try {
            judge0Response = restTemplate.postForObject(judge0ApiUrl, requestEntity, Judge0ResponseDto.class);
            if (judge0Response == null) {
                throw new IllegalStateException("Judge0으로부터 응답을 받지 못했습니다.");
            }
        } catch (HttpClientErrorException e) {
            // Judge0 API 호출 중 에러 발생 시
            throw new IllegalStateException("채점 서버 호출에 실패했습니다: " + e.getResponseBodyAsString(), e);
        }

        // 5. 제출 기록 생성 및 저장
        Submission submission = new Submission();
        submission.setMember(member);
        submission.setQuizId(requestDto.getQuizId());
        submission.setAnswer(requestDto.getAnswer());
        submission.setLanguage(requestDto.getLanguage());
        submission.setStatus(judge0Response.getStatus().getDescription());
        submission.setRunTime(judge0Response.getTime());
        submission.setMemory(judge0Response.getMemory());

        // Judge0 결과에 따라 stdout 또는 stderr/compile_output 저장
        if (judge0Response.getStatus().getId() <= 2) { // In Queue or Processing
            submission.setStdout("채점 중입니다...");
        } else if (judge0Response.getStatus().getId() == 6) { // Compilation Error
            submission.setStderr(judge0Response.getCompileOutput());
        } else {
            submission.setStdout(judge0Response.getStdout());
            submission.setStderr(judge0Response.getStderr());
        }

        Submission savedSubmission = submissionRepository.save(submission);

        // 6. 최종 응답 DTO 생성 및 반환
        return SubmissionResponseDto.builder()
                .submissionId(savedSubmission.getSubmissionId())
                .quizId(savedSubmission.getQuizId())
                .answer(savedSubmission.getAnswer())
                .language(savedSubmission.getLanguage())
                .status(savedSubmission.getStatus())
                .stdout(savedSubmission.getStdout())
                .stderr(savedSubmission.getStderr())
                .runTime(savedSubmission.getRunTime())
                .memory(savedSubmission.getMemory())
                .submittedAt(savedSubmission.getSubmittedAt())
                .build();
    }

    private int getLanguageId(String language) {
        Integer languageId = LANGUAGE_MAP.get(language);
        if (languageId == null) {
            throw new IllegalArgumentException("지원하지 않는 언어입니다: " + language);
        }
        return languageId;
    }
}
