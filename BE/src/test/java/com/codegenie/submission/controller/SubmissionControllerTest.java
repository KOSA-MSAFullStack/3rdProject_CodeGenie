// SubmissionControllerTest.java
// [제출 컨트롤러 테스트]
/*
 * 설명:
 * SubmissionController의 API 엔드포인트를 단위 테스트
 * MockMvc를 사용하여 실제 HTTP 요청처럼 테스트하고, 응답 코드와 본문을 검증
 * @WebMvcTest를 사용하여 웹 레이어에만 집중하고, 서비스 및 리포지토리 레이어는 @MockBean으로 격리
 *
 * 주요 기능:
 * - POST /api/submissions/judge: 코드 제출 API 테스트
 * - GET /api/submissions: 제출 기록 조회 API 테스트
 */

package com.codegenie.submission.controller;

import com.codegenie.member.config.SecurityConfig;
import com.codegenie.member.entity.MemberEntity;
import com.codegenie.member.jwt.JWTUtil;
import com.codegenie.member.repository.MemberRepository;
import com.codegenie.submission.dto.SubmissionDTO;
import com.codegenie.submission.entity.Submission;
import com.codegenie.submission.repository.SubmissionRepository;
import com.codegenie.submission.service.SubmissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SubmissionController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JWTUtil.class)
    })
class SubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public SubmissionService submissionService() {
            return Mockito.mock(SubmissionService.class);
        }

        @Bean
        public MemberRepository memberRepository() {
            return Mockito.mock(MemberRepository.class);
        }

        @Bean
        public SubmissionRepository submissionRepository() {
            return Mockito.mock(SubmissionRepository.class);
        }
    }

    private MemberEntity testMember;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 설정
        testMember = new MemberEntity();
        testMember.setMember_id(1);
        testMember.setEmail("test@example.com");

        // Mock 객체 설정: 컨트롤러의 current() 메소드가 memberRepository를 사용하므로 필요
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(testMember));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/submissions/judge 요청 시, 채점 결과를 정상적으로 반환한다")
    void submit_ShouldReturnJudgeResult() throws Exception {
        // given
        SubmissionDTO.SubmitRequest request = new SubmissionDTO.SubmitRequest();
        request.setQuizId(101);
        request.setAnswer("Some code");

        SubmissionDTO.SubmitResponse response = SubmissionDTO.SubmitResponse.builder()
                .status("Accepted")
                .build();

        given(submissionService.judge(any(MemberEntity.class), any(SubmissionDTO.SubmitRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/submissions/judge")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Accepted"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("GET /api/submissions?quizId={quizId} 요청 시, 제출 기록 목록을 반환한다")
    void listByQuiz_ShouldReturnSubmissionHistory() throws Exception {
        // given
        int quizId = 101;
        Submission submission = Submission.builder()
                .id(1)
                .status("Accepted")
                .submittedAt(LocalDateTime.now())
                .build();

        given(submissionRepository.findByMemberIdAndQuizIdOrderBySubmittedAtDesc(anyInt(), anyInt()))
                .willReturn(Collections.singletonList(submission));

        // when & then
        mockMvc.perform(get("/api/submissions").param("quizId", String.valueOf(quizId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("Accepted"));
    }
}
