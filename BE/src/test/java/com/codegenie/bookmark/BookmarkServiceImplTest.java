// BookmarkServiceImplTest.java
// [테스트] 북마크 서비스 로직
/*
 * 설명:
 * - BookmarkServiceImpl의 비즈니스 로직을 검증하는 단위 테스트
 * - Mockito를 사용하여 의존성을 격리하고 순수 서비스 로직만 테스트
 *
 * 주요 기능:
 * - 북마크 토글 기능 테스트
 * - 저장된 북마크 조회 기능 테스트 (quizNumber, spec, topic 포함)
 */
package com.codegenie.bookmark;

import com.codegenie.bookmark.service.BookmarkServiceImpl;
import com.codegenie.member.entity.MemberEntity;
import com.codegenie.member.repository.MemberRepository;
import com.codegenie.submission.mapper.SubmissionMapper;
import com.codegenie.submission.repository.SubmissionRepository;
import com.codegenie.workbook.entity.CodingQuiz;
import com.codegenie.workbook.entity.Workbook;
import com.codegenie.workbook.repository.CodingQuizRepository;
import com.codegenie.workbook.repository.WorkbookRepository;
import com.codegenie.bookmark.dto.BookmarkDetailDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceImplTest {

    private BookmarkServiceImpl bookmarkService;

    @Mock
    private CodingQuizRepository codingQuizRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private WorkbookRepository workbookRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private SubmissionMapper submissionMapper; // 추가

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // SecurityContextHolder 모의 설정
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(authentication.getName()).thenReturn("test@test.com");

        // BookmarkServiceImpl을 수동으로 초기화하고 모든 Mock 객체를 주입
        bookmarkService = new BookmarkServiceImpl(codingQuizRepository, workbookRepository, memberRepository, submissionRepository, submissionMapper);
    }

    @Test
    @DisplayName("저장된 북마크 조회 성공 테스트")
    void getBookmarks_success() {
        // Given
        MemberEntity member = new MemberEntity();
        Workbook workbook1 = Workbook.builder().id(1).topic("Java 기초").member(member).build();
        
        CodingQuiz quiz1 = CodingQuiz.builder().id(101).workbook(workbook1).quiz("[문제]...").isSaved(false).build();
        CodingQuiz quiz2 = CodingQuiz.builder().id(102).workbook(workbook1).quiz("[문제]...").isSaved(true).build();
        CodingQuiz quiz3 = CodingQuiz.builder().id(103).workbook(workbook1).quiz("[문제]...").isSaved(true).build();
        List<CodingQuiz> allQuizzesInWorkbook1 = List.of(quiz1, quiz2, quiz3);
        List<CodingQuiz> savedQuizzes = List.of(quiz2, quiz3);

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(workbookRepository.findAllByMember(member)).thenReturn(List.of(workbook1)); // 추가
        when(codingQuizRepository.findAllByWorkbookInAndIsSaved(any(), eq(true))).thenReturn(savedQuizzes);
        when(codingQuizRepository.findByWorkbookIdOrderByIdAsc(workbook1.getId())).thenReturn(allQuizzesInWorkbook1);

        // quiz2에 대한 Mock 설정
        when(submissionRepository.countByCodingQuiz(quiz2)).thenReturn(10L);
        when(submissionRepository.countByCodingQuizAndStatus(quiz2, "Accepted")).thenReturn(5L);

        // quiz3에 대한 Mock 설정
        when(submissionRepository.countByCodingQuiz(quiz3)).thenReturn(20L);
        when(submissionRepository.countByCodingQuizAndStatus(quiz3, "Accepted")).thenReturn(15L);

        // When
        List<BookmarkDetailDTO> result = bookmarkService.getBookmarks();

        // Then
        assertThat(result).hasSize(2);

        // 첫 번째 북마크 (quiz2) 검증
        BookmarkDetailDTO resultQuiz2 = result.get(0);
        assertThat(resultQuiz2.getQuiz().getId()).isEqualTo(quiz2.getId());
        assertThat(resultQuiz2.getQuiz().getWorkbookTopic()).isEqualTo("Java 기초");
        assertThat(resultQuiz2.getQuiz().getQuizNumber()).isEqualTo(2); // quiz1, quiz2, quiz3 중 2번째
        assertThat(resultQuiz2.getQuiz().getSpec().submissions()).isEqualTo(10L);
        assertThat(resultQuiz2.getQuiz().getSpec().accepted()).isEqualTo(5L);

        // 두 번째 북마크 (quiz3) 검증
        BookmarkDetailDTO resultQuiz3 = result.get(1);
        assertThat(resultQuiz3.getQuiz().getId()).isEqualTo(quiz3.getId());
        assertThat(resultQuiz3.getQuiz().getWorkbookTopic()).isEqualTo("Java 기초");
        assertThat(resultQuiz3.getQuiz().getQuizNumber()).isEqualTo(3); // quiz1, quiz2, quiz3 중 3번째
        assertThat(resultQuiz3.getQuiz().getSpec().submissions()).isEqualTo(20L);
        assertThat(resultQuiz3.getQuiz().getSpec().accepted()).isEqualTo(15L);
    }

    @Test
    @DisplayName("북마크 토글 테스트 - 추가 및 해제")
    void toggleBookmark_addAndRemove() {
        // Given
        CodingQuiz quiz = CodingQuiz.builder().id(1).isSaved(false).build();
        when(codingQuizRepository.findById(1)).thenReturn(Optional.of(quiz));

        // When (북마크 추가)
        bookmarkService.toggleBookmark(1);
        // Then
        assertThat(quiz.getIsSaved()).isTrue();

        // When (북마크 해제)
        bookmarkService.toggleBookmark(1);
        // Then
        assertThat(quiz.getIsSaved()).isFalse();
    }
}
