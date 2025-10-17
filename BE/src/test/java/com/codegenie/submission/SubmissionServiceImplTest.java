// SubmissionServiceImplTest.java
// [테스트] 문제 제출 서비스 로직
/*
 * 설명:
 * - SubmissionServiceImpl 비즈니스 로직 '단위 테스트'
 * - 의존하는 Repository, Mapper, RestTemplate 등은 Mock 객체로 대체
 *
 * 주요 기능:
 * - 답안 제출 성공 케이스 테스트
 */

package com.codegenie.submission;

import com.codegenie.member.entity.MemberEntity;
import com.codegenie.member.repository.MemberRepository;
import com.codegenie.workbook.entity.CodingQuiz;
import com.codegenie.workbook.repository.CodingQuizRepository;

import java.util.Optional;
//import com.codegenie.submission.dto.Judge0RequestDto;
import com.codegenie.submission.dto.Judge0ResponseDto;
import com.codegenie.submission.dto.SubmissionRequestDto;
import com.codegenie.submission.dto.SubmissionResponseDto;
import com.codegenie.submission.entity.Submission;
import com.codegenie.submission.mapper.SubmissionMapper;
import com.codegenie.submission.repository.SubmissionRepository;
import com.codegenie.submission.service.SubmissionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.NoSuchElementException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

// * author: 김기성
@ExtendWith(MockitoExtension.class)
class SubmissionServiceImplTest {

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CodingQuizRepository codingQuizRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SubmissionMapper submissionMapper;

    @Test
    @DisplayName("답안 제출 성공 테스트")
    void submitAnswer_success() {
        // Given (테스트 조건 설정)
        Integer memberId = 1;
        SubmissionRequestDto requestDto = new SubmissionRequestDto();
        requestDto.setAnswer("print(\"hello world\")");
        requestDto.setLanguage("Python");
        requestDto.setQuizId(1); // Add a valid quizId for the test

        MemberEntity mockMember = new MemberEntity();
        mockMember.setMember_id(memberId);
        mockMember.setEmail("test@test.com");

        Judge0ResponseDto.Status status = new Judge0ResponseDto.Status();
        status.setId(3);
        status.setDescription("Accepted");

        Judge0ResponseDto judge0Response = new Judge0ResponseDto();
        judge0Response.setStdout("hello world");
        judge0Response.setTime(0.1);
        judge0Response.setMemory(1024);
        judge0Response.setStatus(status);

        Submission submission = new Submission();
        Submission savedSubmission = new Submission();
        SubmissionResponseDto expectedResponse = new SubmissionResponseDto();

        // @Value 필드 수동 주입
        ReflectionTestUtils.setField(submissionService, "judge0ApiUrl", "http://fake-judge0-api.com");

        // Mock 객체 행동 정의
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(codingQuizRepository.findById(any(Integer.class))).thenReturn(Optional.of(new CodingQuiz()));
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(Judge0ResponseDto.class))).thenReturn(judge0Response);
        when(submissionMapper.toEntity(any(SubmissionRequestDto.class))).thenReturn(submission);
        when(submissionRepository.save(any(Submission.class))).thenReturn(savedSubmission);
        when(submissionMapper.toDto(any(Submission.class))).thenReturn(expectedResponse);

        // When (테스트 실행)
        SubmissionResponseDto actualResponse = submissionService.submitAnswer(requestDto, memberId);

        // Then (결과 검증)
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(memberRepository).findById(memberId);
        verify(restTemplate).postForObject(anyString(), any(HttpEntity.class), eq(Judge0ResponseDto.class));
        verify(submissionRepository).save(any(Submission.class));
    }

    @Test
    @DisplayName("답안 제출 시 사용자를 찾을 수 없는 경우 예외 발생")
    void submitAnswer_memberNotFound() {
        // Given
        Integer memberId = 999; // 존재하지 않는 사용자 ID
        SubmissionRequestDto requestDto = new SubmissionRequestDto();
        requestDto.setAnswer("code");
        requestDto.setLanguage("Java");
        requestDto.setQuizId(1); // 추가
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> {
            submissionService.submitAnswer(requestDto, memberId);
        });
        verify(memberRepository).findById(memberId);
    }
}
