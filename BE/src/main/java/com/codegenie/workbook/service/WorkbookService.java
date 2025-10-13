package com.codegenie.workbook.service;

import com.codegenie.workbook.dto.CreateWorkbookRequest;
import com.codegenie.workbook.dto.WorkbookResponse;
import com.codegenie.workbook.entity.CodingQuiz;
import com.codegenie.workbook.entity.Workbook;

import java.util.List;

public interface WorkbookService {
    WorkbookResponse create(CreateWorkbookRequest req);              // 문제집 생성(+퀴즈 10개)
    Workbook getOne(Long id);                                        // 단건
    List<CodingQuiz> getQuizzes(Long workbookId);                    // 문제 목록
    List<Workbook> getRecent(int limit);                             // 최근 N개
}
