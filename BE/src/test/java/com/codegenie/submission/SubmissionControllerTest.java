// SubmissionControllerTest.java
// [테스트] 문제 제출 API
/*
 * 설명:
 * - SubmissionController API 엔드포인트 '통합 테스트'
 * - MockMvc 사용, 실제 HTTP 요청 시뮬레이션
 * - Service 계층은 MockBean으로 대체
 *
 * 주요 기능:
 * - 답안 제출 API 성공 케이스 테스트
 */

package com.codegenie.submission;

import com.codegenie.member.dto.CustomUserDetails;
import com.codegenie.member.entity.MemberEntity;
import com.codegenie.member.service.CustomUserDetailsService;
import com.codegenie.submission.controller.SubmissionController;
import com.codegenie.submission.dto.SubmissionRequestDto;
import com.codegenie.submission.dto.SubmissionResponseDto;
import com.codegenie.submission.service.SubmissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// * author: 김기성
@WebMvcTest(controllers = SubmissionController.class)
class SubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubmissionService submissionService;

    // Spring Security가 내부적으로 의존하므로 MockBean으로 유지
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/submission - 답안 제출 성공")
    void submitAnswer_success() throws Exception {
        // Given
        // 1. 가짜 MemberEntity 및 CustomUserDetails(인증된 사용자 정보) 생성
        MemberEntity mockMember = new MemberEntity();
        mockMember.setMember_id(1);
        mockMember.setEmail("test@test.com");
        CustomUserDetails mockUserDetails = new CustomUserDetails(mockMember);

        // 2. 요청 및 응답 DTO 생성
        SubmissionRequestDto requestDto = new SubmissionRequestDto();
        requestDto.setAnswer("print(\"hello\")");
        requestDto.setLanguage("Python");

        SubmissionResponseDto responseDto = new SubmissionResponseDto();
        responseDto.setStatus("Accepted");

        // 3. Mock Service 행동 정의
        when(submissionService.submitAnswer(any(SubmissionRequestDto.class), anyInt())).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/submission")
                        .with(csrf()) // CSRF 토큰 처리
                        .with(user(mockUserDetails)) // 4. 가짜 사용자 정보를 요청에 포함시켜 인증 통과
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Accepted"));
    }
}
