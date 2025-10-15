package com.codegenie.workbook.service;

import com.codegenie.workbook.dto.CreateWorkbookRequest;
import com.codegenie.workbook.dto.WorkbookResponse;
import com.codegenie.workbook.view.QuizView;

import java.util.List;

public interface WorkbookService {
    WorkbookResponse create(CreateWorkbookRequest req);
    WorkbookResponse getOneDto(Integer id);
    List<QuizView> getQuizzes(Integer workbookId);
    List<WorkbookResponse> getRecent(int limit);
}
