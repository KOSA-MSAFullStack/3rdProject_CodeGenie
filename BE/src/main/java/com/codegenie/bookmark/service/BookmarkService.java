package com.codegenie.bookmark.service;

import com.codegenie.bookmark.dto.BookmarkDetailDTO;
import com.codegenie.bookmark.dto.BookmarkedQuizViewDTO;

import java.util.List;

public interface BookmarkService {
    void toggleBookmark(Integer quizId);

    List<BookmarkDetailDTO> getBookmarks();

    List<BookmarkedQuizViewDTO> getQuizzesForWorkbook(Integer workbookId);

    List<String> getWorkbookTopicsWithBookmarks();
}
