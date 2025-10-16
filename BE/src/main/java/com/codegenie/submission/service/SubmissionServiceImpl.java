// SubmissionServiceImpl.java
// 문제 제출 '비즈니스 로직 구현체'
/*
 * 설명:
 * - SubmissionService 인터페이스를 구현한 클래스
 * - 실제 답안 제출 및 채점 요청 로직을 담당
 * 
 * 주요 기능:
 * - 답안 제출 로직 구현
 */

package com.codegenie.submission.service;

import com.codegenie.workbook.entity.CodingQuiz;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.codegenie.workbook.repository.CodingQuizRepository;
import com.codegenie.submission.mapper.SubmissionMapper;
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
import lombok.extern.slf4j.Slf4j;

// * author: 김기성
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final MemberRepository memberRepository;
    private final CodingQuizRepository codingQuizRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SubmissionMapper submissionMapper;    // (1) Mapper 주입

    @Value("${judge0.api.url}")
    private String judge0ApiUrl;

    // 지원하는 언어와 Judge0 ID 매핑
    private static final Map<String, Integer> LANGUAGE_MAP = Map.of(
            "Java", 62,
            "Python", 71,
            "C++", 54);

    @Override
    public SubmissionResponseDto submitAnswer(SubmissionRequestDto requestDto, Integer memberId) {
        // 0) 입력 유효성 검증: quizId 필수
        if (requestDto.getQuizId() == null) {
            throw new IllegalArgumentException("quizId는 필수입니다.");
        }

        // 1. 사용자 및 문제 조회
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("해당 사용자를 찾을 수 없습니다. ID: " + memberId));
        CodingQuiz codingQuiz = codingQuizRepository.findById(requestDto.getQuizId())
                .orElseThrow(() -> new NoSuchElementException("해당 문제를 찾을 수 없습니다. ID: " + requestDto.getQuizId()));

        // 2. 언어 ID 조회
        int languageId = getLanguageId(requestDto.getLanguage());

        // 3. Judge0 API 요청 생성
        Judge0RequestDto judge0Request = new Judge0RequestDto(requestDto.getAnswer(), languageId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 4. Judge0 API 호출 (RestTemplate 내부 직렬화 문제 우회를 위해 수동으로 JSON 변환)
        Judge0ResponseDto judge0Response;
        try {
            String jsonPayload = objectMapper.writeValueAsString(judge0Request);
            log.info("Judge0 API 요청 페이로드: {}", jsonPayload);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);

            judge0Response = restTemplate.postForObject(judge0ApiUrl, requestEntity, Judge0ResponseDto.class);
            log.info("Judge0 API 응답: {}", judge0Response); // Judge0 응답 로깅
            if (judge0Response == null) {
                throw new IllegalStateException("Judge0으로부터 응답을 받지 못했습니다.");
            }
        } catch (HttpClientErrorException e) {
            // 4xx, 5xx 같은 HTTP 에러 응답을 받은 경우
            log.error("채점 서버 요청 실패: {}, 응답 본문: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("채점 서버가 요청을 처리하는 중 오류를 반환했습니다: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            // 네트워크 연결 실패 등 RestTemplate 호출의 근본적인 문제
            log.error("채점 서버 연결 또는 JSON 처리 실패", e);
            throw new IllegalStateException("채점 서버에 연결할 수 없습니다. Docker가 실행 중인지, 네트워크 설정을 확인해주세요.", e);
        }

        // 5. 제출 기록 생성 및 저장
        Submission submission = submissionMapper.toEntity(requestDto);      // (2) DTO -> Entity 변환
        submission.setMember(member);
        submission.setCodingQuiz(codingQuiz); // 문제 연결

        // Judge0 응답 상태 확인 및 설정
        String submissionStatus = "Internal Error"; // 기본값
        if (judge0Response.getStatus() != null) {
            submissionStatus = judge0Response.getStatus().getDescription();
            log.info("Judge0 응답 상태: {}", submissionStatus);
        } else {
            log.warn("Judge0 응답에 상태 정보가 없습니다. Judge0ResponseDto: {}", judge0Response);
        }
        submission.setStatus(submissionStatus);
        submission.setRunTime(judge0Response.getTime());
        submission.setMemory(judge0Response.getMemory());

        // Judge0 결과에 따라 stdout 또는 stderr/compile_output 저장 (status null 안전 처리)
        if (judge0Response.getStatus() == null) {
            submission.setStdout(judge0Response.getStdout());
            submission.setStderr(judge0Response.getStderr());
        } else {
            int statusId = judge0Response.getStatus().getId();
            if (statusId <= 2) { // In Queue or Processing
                submission.setStdout("채점 중입니다...");
            } else if (statusId == 6) { // Compilation Error
                submission.setStderr(judge0Response.getCompileOutput());
            } else {
                submission.setStdout(judge0Response.getStdout());
                submission.setStderr(judge0Response.getStderr());
            }
        }

        Submission savedSubmission = submissionRepository.save(submission);     // (3) DB에 저장

        // 6. 최종 응답 DTO 생성 및 반환
        return submissionMapper.toDto(savedSubmission);     // (4) Entity -> DTO 변환 후 반환
    }

    private int getLanguageId(String language) {
        Integer languageId = LANGUAGE_MAP.get(language);
        if (languageId == null) {
            throw new IllegalArgumentException("지원하지 않는 언어입니다: " + language);
        }
        return languageId;
    }
}
