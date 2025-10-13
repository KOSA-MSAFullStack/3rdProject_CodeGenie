package com.codegenie.workbook.service;

import com.codegenie.workbook.dto.CreateWorkbookRequest;
import com.codegenie.workbook.dto.QuizResponse;
import com.codegenie.workbook.dto.WorkbookResponse;
import com.codegenie.workbook.entity.CodingQuiz;
import com.codegenie.workbook.entity.Workbook;
import com.codegenie.workbook.repository.CodingQuizRepository;
import com.codegenie.workbook.repository.WorkbookRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.client.ChatClient;                 // ✅ 변경
import org.springframework.ai.chat.model.ChatResponse;           // ✅ 패키지 주의
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Comparator;

/**
 * 문제집 생성: 프론트의 폼(언어/난이도/학습스타일/요청상세/주제) → Spring AI 호출 → JSON 10문제 파싱 → DB 저장 → 응답 DTO
 */
@Service
@RequiredArgsConstructor
public class WorkbookServiceImpl implements WorkbookService {

    private final WorkbookRepository workbookRepository;
    private final CodingQuizRepository codingQuizRepository;

    // ❌ (삭제) private final OpenAiChatClient chatClient;
    private final ChatClient chatClient;                           // ✅ ChatClient 로 교체

    private final ObjectMapper objectMapper = new ObjectMapper();  // JSON 파서

    @Override
    @Transactional
    public WorkbookResponse create(CreateWorkbookRequest req) {
        // 1) 문제집 생성/저장 (엔티티)
        Workbook wb = Workbook.builder()
                .language(req.getLanguage())
                .level(req.getLevel())
                .style(req.getStyle())
                .requestDetail(req.getRequestDetail())
                .topic(req.getTopic())
                .createdAt(LocalDateTime.now()) // 엔티티 필드에만 사용, 응답 DTO에는 포함 안 함
                .build();
        workbookRepository.save(wb);

        // 2) 프롬프트 작성(난이도/스타일 가이드 명시)
        String promptText = buildPrompt(req);

        // 3) OpenAI 호출 + JSON 파싱
        List<AiQuizDTO> aiQuizzes = callOpenAiAndParse(promptText);

        // 4) 실패/비어있으면 안전한 더미 10개
        if (aiQuizzes == null || aiQuizzes.isEmpty()) {
            aiQuizzes = fallbackDummy();
        }

        // 5) DB 저장
        List<CodingQuiz> saved = new ArrayList<>();
        int order = 1;
        for (AiQuizDTO q : aiQuizzes) {
            CodingQuiz entity = CodingQuiz.builder()
                    .workbook(wb)
                    .orderNo(q.orderNo() != null ? q.orderNo() : order)
                    .qname(defaultIfBlank(q.qname(), "문제 " + order))
                    .statement(defaultIfBlank(q.statement(), "두 정수 A와 B를 입력받아 A+B를 출력하세요."))
                    .inputText(defaultIfBlank(q.inputText(), "첫째 줄에 A와 B가 주어진다. (0 < A, B < 10)"))
                    .outputText(defaultIfBlank(q.outputText(), "첫째 줄에 A+B를 출력한다."))
                    .sampleInput(defaultIfBlank(q.sampleInput(), "1 2"))
                    .explanation(defaultIfBlank(q.explanation(), "표준입출력 + 기본 연산 개념을 익혀봅니다."))
                    .concept(defaultIfBlank(q.concept(), "입출력,연산자,정수"))
                    .submissions(0L)
                    .accepted(0L)
                    .build();
            codingQuizRepository.save(entity);
            saved.add(entity);
            order++;
        }
        saved.sort(Comparator.comparing(CodingQuiz::getOrderNo));

        // 6) 응답 DTO 조립 (프론트 카드 포맷)
        return WorkbookResponse.builder()
                .id(wb.getId())
                .topic(wb.getTopic())
                .language(wb.getLanguage())
                .level(wb.getLevel())
                .style(wb.getStyle())
                .requestDetail(wb.getRequestDetail())
                .quizzes(saved.stream().map(this::toQuizResponse).toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Workbook getOne(Long id) {
        return workbookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workbook not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodingQuiz> getQuizzes(Long workbookId) {
        return codingQuizRepository.findByWorkbookIdOrderByOrderNoAsc(workbookId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Workbook> getRecent(int limit) {
        var all = workbookRepository.findAll();
        all.sort((a, b) -> Long.compare(b.getId(), a.getId())); // ID desc
        return all.stream().limit(limit).toList();
    }

    /* ============================== Private Helpers ============================== */

    // 🔥 강화된 프롬프트: 난이도/스타일 가이드 명시 + 출력 포맷 고정
    private String buildPrompt(CreateWorkbookRequest req) {
        String level = nullSafe(req.getLevel());
        String style = nullSafe(req.getStyle());
        String language = nullSafe(req.getLanguage());
        String topic = nullSafe(req.getTopic());
        String detail = nullSafe(req.getRequestDetail());

        String levelRubric = switch (level) {
            case "초급" -> """
                - 난이도: 초급
                - 요구: 기초 문법/입출력/조건/반복/간단 자료구조(배열/리스트) 중심
                - 금지: 복잡한 최적화, 고급 알고리즘(세그먼트트리/다익스트라 등) 언급 금지
                - 예시 유형: 합계/최댓값/문자열 기초/간단 구현
                """;
            case "중급" -> """
                - 난이도: 중급
                - 요구: 정렬/해시/스택/큐/문자열 처리/기본 알고리즘(투포인터, BFS/DFS) 중심
                - 금지: 과도한 난이도 상승(고급 자료구조/복잡 DP 배제)
                - 예시 유형: 빈도수 계산, 구간합, 간단 그래프 탐색
                """;
            case "고급" -> """
                - 난이도: 고급
                - 요구: 그래프(최단경로, 위상정렬), 고급 자료구조, DP, 조합 탐색/최적화
                - 예시 유형: 경로 최적화, 상태 DP, 특수 자료구조 개념 활용
                """;
            default -> """
                - 난이도: 미지정(중간 수준으로 균형 있게 구성)
                """;
        };

        String styleRubric = switch (style) {
            case "간단요약" -> """
                - 학습 스타일: 간단요약
                - 해설은 핵심 포인트만 4~6줄 이내 요약
                - 개념은 콤마(,)로 3~6개 키워드만 제시
                """;
            case "깊이설명" -> """
                - 학습 스타일: 깊이설명
                - 해설은 접근 아이디어 → 복잡도 → 코너케이스 순(8~12줄)
                - 개념에는 핵심 이론 + 오용 주의점 포함(콤마로 4~8개)
                """;
            case "예시중심" -> """
                - 학습 스타일: 예시중심
                - 해설은 작은 예제 1~2개로 단계별 풀이 설명(6~10줄)
                - sampleInput은 실제 이해에 도움이 되는 값
                """;
            default -> """
                - 학습 스타일: 일반
                """;
        };

        return ("""
            역할: 당신은 한국어로 답변하는 %s 튜터입니다. 아래 입력을 반영해 '코딩 연습 문제 10개'를 생성하세요.
            반드시 "출력 형식 규칙"만 지키고, 그 외 텍스트/마크다운/코드펜스는 절대 쓰지 마세요.

            [사용자 입력]
            - 언어: %s
            - 난이도: %s
            - 학습 스타일: %s
            - 주제: %s
            - 요청 상세(추천 개념/기타 요구사항): %s

            [난이도 가이드]
            %s

            [학습 스타일 가이드]
            %s

            [출력 형식 규칙]
            - 출력은 오직 JSON 배열 한 덩어리만: 크기 정확히 10.
            - 각 원소는 아래 모든 필드를 포함:
              {
                "orderNo": 1..10 (정수),
                "qname": "문제 1..문제 10 형식의 간단 제목",
                "statement": "문제 설명 (한국어, 4~8줄)",
                "inputText": "입력 형식 설명 (1~3줄)",
                "outputText": "출력 형식 설명 (1~2줄)",
                "sampleInput": "예제 입력 (간단 값)",
                "explanation": "해설 (스타일 가이드 준수)",
                "concept": "관련 개념 키워드(콤마로 구분, 3~8개)"
              }
            - 문자열 내 따옴표/개행 등은 유효한 JSON으로 이스케이프.
            - orderNo는 1부터 10까지 증가.
            - %s 언어로 풀이 아이디어를 안내하되, 실제 코드 출력은 하지 않음.
            """).formatted(
                language,            // 역할 문구
                language, level, style, topic, detail,
                levelRubric, styleRubric,
                language
        );
    }

    private List<AiQuizDTO> callOpenAiAndParse(String promptText) {
        try {
            // ✅ ChatClient 사용 (Prompt/Message 그대로 사용 가능)
            ChatResponse response = chatClient
                    .prompt(new Prompt(new UserMessage(promptText)))
                    .call()
                    .chatResponse();

            String content = response.getResult().getOutput().getContent();
            String json = sanitizeToJsonArray(content); // ```json 제거 + 배열만 추출
            return objectMapper.readValue(json, new TypeReference<List<AiQuizDTO>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 파싱 실패 → fallbackDummy() 사용
        }
    }

    // 백틱 코드펜스 제거 + 대괄호로 감싼 JSON 배열만 추출
    private String sanitizeToJsonArray(String raw) {
        if (raw == null) return "[]";
        String s = raw.trim();
        // 코드펜스 제거
        s = s.replace("```json", "").replace("```", "").trim();
        // 배열만 추출
        int start = s.indexOf('[');
        int end = s.lastIndexOf(']');
        if (start >= 0 && end > start) {
            s = s.substring(start, end + 1);
        }
        if (!s.startsWith("[")) return "[]";
        return s;
    }

    private List<AiQuizDTO> fallbackDummy() {
        List<AiQuizDTO> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            list.add(new AiQuizDTO(
                    i,
                    "문제 " + i,
                    "두 정수 A와 B를 입력받아 A+B를 출력하세요.",
                    "첫째 줄에 A와 B가 주어진다. (0 < A, B < 10)",
                    "첫째 줄에 A+B를 출력한다.",
                    "1 2",
                    "표준입출력과 정수 덧셈을 연습합니다.",
                    "입출력,연산자,정수"
            ));
        }
        return list;
    }

    // 엔티티 -> 응답 DTO
    private QuizResponse toQuizResponse(CodingQuiz q) {
        return QuizResponse.builder()
                .id(q.getId())
                .orderNo(q.getOrderNo())
                .qname(q.getQname())
                .statement(q.getStatement())
                .inputText(q.getInputText())
                .outputText(q.getOutputText())
                .sampleInput(q.getSampleInput())
                .explanation(q.getExplanation())
                .concept(q.getConcept())
                .submissions(q.getSubmissions())
                .accepted(q.getAccepted())
                .build();
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
    private static String defaultIfBlank(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    /* OpenAI 응답 JSON 매핑용 DTO (필요 필드만; 여분은 무시) */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AiQuizDTO(
            Integer orderNo,
            String qname,
            String statement,
            String inputText,
            String outputText,
            String sampleInput,
            String explanation,
            String concept
    ) {}
}
