package com.codegenie.workbook.controller;

import com.codegenie.workbook.dto.CreateWorkbookRequest;
import com.codegenie.workbook.dto.WorkbookResponse;
import com.codegenie.workbook.service.WorkbookService;
import com.codegenie.workbook.view.QuizView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workbooks")
@CrossOrigin(origins = "http://localhost:5173")
public class WorkbookController {

    private final WorkbookService workbookService;

    /** 문제집 생성 */
    @PostMapping
    public ResponseEntity<WorkbookResponse> create(@RequestBody CreateWorkbookRequest req) {
        return ResponseEntity.ok(workbookService.create(req));
    }

    /** 문제집 단건 조회 - DTO 반환 */
    @GetMapping("/{id}")
    public ResponseEntity<WorkbookResponse> getWorkbook(@PathVariable Integer id) {
        return ResponseEntity.ok(workbookService.getOneDto(id));
    }

    /** 문제 리스트 조회 (뷰 DTO) */
    @GetMapping("/{id}/quizzes")
    public ResponseEntity<List<QuizView>> getQuizzes(@PathVariable Integer id) {
        return ResponseEntity.ok(workbookService.getQuizzes(id));
    }

    /** 최근 N개 - DTO 반환 (사이드바/초기 표시에 사용) */
    @GetMapping
    public ResponseEntity<List<WorkbookResponse>> getRecent(@RequestParam(defaultValue = "1") int limit) {
        return ResponseEntity.ok(workbookService.getRecent(limit));
    }
}
