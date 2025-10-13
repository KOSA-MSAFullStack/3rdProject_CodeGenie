package com.codegenie.workbook.controller;

import com.codegenie.workbook.dto.CreateWorkbookRequest;
import com.codegenie.workbook.dto.WorkbookResponse;
import com.codegenie.workbook.entity.CodingQuiz;
import com.codegenie.workbook.entity.Workbook;
import com.codegenie.workbook.service.WorkbookService;
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

    /** 문제집 단건 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<Workbook> getWorkbook(@PathVariable Long id) {
        return ResponseEntity.ok(workbookService.getOne(id));
    }

    /** 문제 리스트 조회 */
    @GetMapping("/{id}/quizzes")
    public ResponseEntity<List<CodingQuiz>> getQuizzes(@PathVariable Long id) {
        return ResponseEntity.ok(workbookService.getQuizzes(id));
    }

    /** 최근 N개 (사이드바/초기 표시에 사용) */
    @GetMapping
    public ResponseEntity<List<Workbook>> getRecent(@RequestParam(defaultValue = "1") int limit) {
        return ResponseEntity.ok(workbookService.getRecent(limit));
    }
}
