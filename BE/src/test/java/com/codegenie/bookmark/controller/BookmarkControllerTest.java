// BookmarkControllerTest.java
// [북마크 컨트롤러 테스트]
/*
 * 설명:
 * BookmarkController의 API 엔드포인트를 단위 테스트
 * MockMvc를 사용하여 실제 HTTP 요청처럼 테스트하고, 응답 코드와 본문을 검증
 * @WebMvcTest를 사용하여 웹 레이어에만 집중하고, 서비스 레이어는 @MockBean으로 격리
 *
 * 주요 기능:
 * - GET /api/bookmarks: 북마크 목록 조회 API 테스트
 * - POST /api/bookmarks/{quizId}: 북마크 추가 API 테스트
 */

package com.codegenie.bookmark.controller;

import com.codegenie.bookmark.dto.BookmarkDTO;
import com.codegenie.bookmark.service.BookmarkService;
import com.codegenie.member.config.SecurityConfig;
import com.codegenie.member.entity.MemberEntity;
import com.codegenie.member.jwt.JWTUtil;
import com.codegenie.member.repository.MemberRepository;
import com.codegenie.workbook.dto.QuizResponse;
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

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookmarkController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JWTUtil.class)
    })
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private MemberRepository memberRepository;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public BookmarkService bookmarkService() {
            return Mockito.mock(BookmarkService.class);
        }

        @Bean
        public MemberRepository memberRepository() {
            return Mockito.mock(MemberRepository.class);
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
    @DisplayName("GET /api/bookmarks/saved 요청 시, 북마크 목록을 정상적으로 반환한다")
    void getSavedBookmarks_ShouldReturnBookmarks() throws Exception {
        // given
        BookmarkDTO bookmarkDTO = BookmarkDTO.builder()
                .quiz(QuizResponse.builder().id(101).quiz("Test Quiz").build())
                .build();
        given(bookmarkService.getBookmarks()).willReturn(Collections.singletonList(bookmarkDTO));

        // when & then
        mockMvc.perform(get("/api/bookmarks/saved"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].quiz.id").value(101));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("POST /api/bookmarks/{quizId} 요청 시, 북마크를 토글하고 200 OK를 반환한다")
    void toggleBookmark_ShouldReturnOk() throws Exception {
        // given
        int quizId = 101;
        doNothing().when(bookmarkService).toggleBookmark(anyInt());

        // when & then
        mockMvc.perform(post("/api/bookmarks/{quizId}", quizId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}