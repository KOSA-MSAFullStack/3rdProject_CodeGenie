// SubmissionServiceTest.java
// [제출 서비스 테스트]
/*
 * 설명:
 * SubmissionServiceImpl의 핵심 비즈니스 로직인 코드 채점(judge) 기능을 단위 테스트
 * LocalJudge와 Repository 의존성을 Mockito로 격리하여 다양한 채점 시나리오를 검증
 *
 * 주요 기능:
 * - 정답, 오답, 컴파일 에러 등 다양한 채점 결과에 따른 로직 처리 테스트
 * - 채점 결과가 DB에 올바르게 저장되는지 검증
 * - 최종 응답 DTO가 정확하게 생성되는지 검증
 */

package com.codegenie.submission.service;

import com.codegenie.member.entity.MemberEntity;
import com.codegenie.submission.dto.SubmissionDTO;
import com.codegenie.submission.entity.Submission;
import com.codegenie.submission.judge.LocalJudge;
import com.codegenie.submission.repository.SubmissionRepository;
import com.codegenie.workbook.entity.CodingQuiz;
import com.codegenie.workbook.entity.Workbook;
import com.codegenie.workbook.repository.CodingQuizRepository;
import com.codegenie.workbooktestcase.entity.QuizTestcase;
import com.codegenie.workbooktestcase.repository.QuizTestcaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private CodingQuizRepository codingQuizRepository;
    @Mock
    private QuizTestcaseRepository quizTestcaseRepository;
    @Mock
    private LocalJudge localJudge;

    private MemberEntity testMember;
    private SubmissionDTO.SubmitRequest submitRequest;
    private CodingQuiz testQuiz;
    private QuizTestcase testcase;

    @BeforeEach
    void setUp() {
        // 테스트용 기본 데이터 설정
        testMember = new MemberEntity();
        testMember.setMember_id(1);

        Workbook workbook = Workbook.builder().language("Java").build();
        testQuiz = CodingQuiz.builder().id(101).workbook(workbook).build();
        
        testcase = new QuizTestcase();
        testcase.setId(1);
        testcase.setQuizId(101);
        testcase.setInputText("1 2");
        testcase.setExpectedOut("3");

        submitRequest = new SubmissionDTO.SubmitRequest();
        submitRequest.setQuizId(101);
        submitRequest.setAnswer("public class Main {}");
        submitRequest.setLanguage("Java");

        // 공통 Mock 설정만 남김
        given(submissionRepository.save(any(Submission.class))).willAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("정답(Accepted)인 경우, 상태와 결과를 올바르게 저장하고 반환한다")
    void judge_WhenAccepted_ShouldSaveAndReturnCorrectly() {
        // given
        given(codingQuizRepository.findById(anyInt())).willReturn(Optional.of(testQuiz));
        given(quizTestcaseRepository.findByQuizIdOrderByIdAsc(anyInt())).willReturn(Collections.singletonList(testcase));
        LocalJudge.JudgeReport report = LocalJudge.JudgeReport.builder().status("Accepted").totalTime(0.123).build();
        given(localJudge.judge(any(), any(), any())).willReturn(report);

        // when
        SubmissionDTO.SubmitResponse response = submissionService.judge(testMember, submitRequest);

        // then
        ArgumentCaptor<Submission> submissionCaptor = ArgumentCaptor.forClass(Submission.class);
        verify(submissionRepository).save(submissionCaptor.capture());
        Submission savedSubmission = submissionCaptor.getValue();

        assertThat(savedSubmission.getStatus()).isEqualTo("Accepted");
        assertThat(savedSubmission.getRunTime()).isEqualTo(0.123);
        assertThat(response.getStatus()).isEqualTo("Accepted");
        assertThat(response.getStdout()).isEqualTo("정답입니다!");
    }

    @Test
    @DisplayName("오답(Wrong Answer)인 경우, 상태와 결과를 올바르게 저장하고 반환한다")
    void judge_WhenWrongAnswer_ShouldSaveAndReturnCorrectly() {
        // given
        given(codingQuizRepository.findById(anyInt())).willReturn(Optional.of(testQuiz));
        given(quizTestcaseRepository.findByQuizIdOrderByIdAsc(anyInt())).willReturn(Collections.singletonList(testcase));
        LocalJudge.JudgeReport report = LocalJudge.JudgeReport.builder().status("Wrong Answer").runLog("Expected: 3, Got: 4").build();
        given(localJudge.judge(any(), any(), any())).willReturn(report);

        // when
        SubmissionDTO.SubmitResponse response = submissionService.judge(testMember, submitRequest);

        // then
        ArgumentCaptor<Submission> submissionCaptor = ArgumentCaptor.forClass(Submission.class);
        verify(submissionRepository).save(submissionCaptor.capture());
        Submission savedSubmission = submissionCaptor.getValue();

        assertThat(savedSubmission.getStatus()).isEqualTo("Wrong Answer");
        assertThat(response.getStatus()).isEqualTo("Wrong Answer");
        assertThat(response.getStderr()).contains("Expected: 3, Got: 4");
    }

    @Test
    @DisplayName("컴파일 에러인 경우, 상태와 로그를 올바르게 저장하고 반환한다")
    void judge_WhenCompilationError_ShouldSaveAndReturnCorrectly() {
        // given
        given(codingQuizRepository.findById(anyInt())).willReturn(Optional.of(testQuiz));
        given(quizTestcaseRepository.findByQuizIdOrderByIdAsc(anyInt())).willReturn(Collections.singletonList(testcase));
        LocalJudge.JudgeReport report = LocalJudge.JudgeReport.builder().status("Compilation Error").compileLog("Syntax error").build();
        given(localJudge.judge(any(), any(), any())).willReturn(report);

        // when
        SubmissionDTO.SubmitResponse response = submissionService.judge(testMember, submitRequest);

        // then
        ArgumentCaptor<Submission> submissionCaptor = ArgumentCaptor.forClass(Submission.class);
        verify(submissionRepository).save(submissionCaptor.capture());
        Submission savedSubmission = submissionCaptor.getValue();

        assertThat(savedSubmission.getStatus()).isEqualTo("Compilation Error");
        assertThat(response.getStatus()).isEqualTo("Compilation Error");
        assertThat(response.getStderr()).isEqualTo("Syntax error");
    }

    @Test
    @DisplayName("코드가 비어있을 경우, 즉시 오답으로 처리하고 저장한다")
    void judge_WhenCodeIsEmpty_ShouldReturnWrongAnswer() {
        // given
        submitRequest.setAnswer(""); // 빈 코드 제출
        // 이 테스트는 다른 Mock이 필요 없음

        // when
        SubmissionDTO.SubmitResponse response = submissionService.judge(testMember, submitRequest);

        // then
        ArgumentCaptor<Submission> submissionCaptor = ArgumentCaptor.forClass(Submission.class);
        verify(submissionRepository).save(submissionCaptor.capture());
        Submission savedSubmission = submissionCaptor.getValue();

        assertThat(savedSubmission.getStatus()).isEqualTo("Wrong Answer");
        assertThat(response.getStatus()).isEqualTo("Wrong Answer");
        assertThat(response.getStderr()).isEqualTo("빈 코드 입니다.");
    }
}