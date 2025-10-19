// BookmarkServiceTest.java
// [북마크 서비스 테스트]
/*
 * 설명:
 * BookmarkServiceImpl의 비즈니스 로직을 단위 테스트
 * Mockito를 사용하여 의존성을 격리하고 서비스 로직의 정확성을 검증
 *
 * 주요 기능:
 * - 북마크 목록 조회 기능 테스트
 * - 북마크 추가/삭제(토글) 기능 테스트
 */

package com.codegenie.bookmark.service;

import com.codegenie.bookmark.dto.BookmarkDTO;
import com.codegenie.member.entity.MemberEntity;
import com.codegenie.member.repository.MemberRepository;
import com.codegenie.submission.entity.Submission;
import com.codegenie.submission.repository.SubmissionRepository;
import com.codegenie.workbook.entity.CodingQuiz;
import com.codegenie.workbook.entity.Workbook;
import com.codegenie.workbook.repository.CodingQuizRepository;
import com.codegenie.workbook.repository.WorkbookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @InjectMocks
    private BookmarkServiceImpl bookmarkService;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private CodingQuizRepository codingQuizRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private WorkbookRepository workbookRepository;

    private MemberEntity testMember;
    private CodingQuiz testQuiz;
    private Workbook testWorkbook;

    @BeforeEach
    void setUp() {
        // 테스트용 기본 데이터 설정
        testMember = new MemberEntity();
        testMember.setMember_id(1);
        testMember.setEmail("test@example.com");

        testWorkbook = Workbook.builder().id(1).topic("Test Workbook").member(testMember).build();
        testQuiz = CodingQuiz.builder()
                .id(101)
                .workbook(testWorkbook)
                .quiz("Test statement")
                .isSaved(true)
                .build();
    }

    @Test
    @DisplayName("북마크 목록 조회 시, 북마크된 퀴즈와 마지막 제출 기록을 함께 반환한다")
    void getBookmarks_ShouldReturnBookmarksWithLastSubmission() {
        // given
        // SecurityContext와 사용자 조회를 위한 Mock 설정
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testMember.getEmail(), "password")
        );
        when(memberRepository.findByEmail(testMember.getEmail())).thenReturn(Optional.of(testMember));

        // 실제 서비스 로직에 맞게 Mock 객체 설정
        when(workbookRepository.findAllByMember(testMember)).thenReturn(Collections.singletonList(testWorkbook));
        when(codingQuizRepository.findAllByWorkbookInAndIsSaved(any(), anyBoolean())).thenReturn(Collections.singletonList(testQuiz));
        when(codingQuizRepository.findByWorkbookIdOrderByIdAsc(testWorkbook.getId())).thenReturn(Collections.singletonList(testQuiz));

        Submission lastSubmission = Submission.builder().answer("Test answer").status("Accepted").submittedAt(LocalDateTime.now()).build();
        when(submissionRepository.findTopByMemberIdAndQuizIdOrderBySubmittedAtDesc(testMember.getMember_id(), testQuiz.getId()))
                .thenReturn(Optional.of(lastSubmission));

        // when
        List<BookmarkDTO> bookmarks = bookmarkService.getBookmarks();

        // then
        assertThat(bookmarks).hasSize(1);
        BookmarkDTO result = bookmarks.get(0);
        assertThat(result.getQuiz().getId()).isEqualTo(testQuiz.getId());
        assertThat(result.getLastSubmission()).isNotNull();
        assertThat(result.getLastSubmission().getStatus()).isEqualTo("Accepted");
    }

    @Test
    @DisplayName("북마크 토글 시, 해당 퀴즈의 isSaved 상태가 변경된다")
    void toggleBookmark_ShouldChangeIsSavedState() {
        // given
        when(codingQuizRepository.findById(testQuiz.getId())).thenReturn(Optional.of(testQuiz));
        testQuiz.setIsSaved(false); // 초기 상태는 false

        // when
        bookmarkService.toggleBookmark(testQuiz.getId());

        // then
        assertThat(testQuiz.getIsSaved()).isTrue(); // 상태가 true로 변경되었는지 확인

        // when
        bookmarkService.toggleBookmark(testQuiz.getId());

        // then
        assertThat(testQuiz.getIsSaved()).isFalse(); // 다시 false로 변경되었는지 확인
    }
}