package com.codegenie.workbook.service;

import com.codegenie.member.entity.MemberEntity;
import com.codegenie.member.repository.MemberRepository;
import com.codegenie.workbook.dto.CreateWorkbookRequest;
import com.codegenie.workbook.dto.QuizResponse;
import com.codegenie.workbook.dto.WorkbookResponse;
import com.codegenie.workbook.entity.CodingQuiz;
import com.codegenie.workbook.entity.Workbook;
import com.codegenie.workbook.repository.CodingQuizRepository;
import com.codegenie.workbook.repository.WorkbookRepository;
import com.codegenie.workbook.view.QuizView;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkbookServiceImpl implements WorkbookService {

    private final WorkbookRepository workbookRepository;
    private final CodingQuizRepository codingQuizRepository;
    private final MemberRepository memberRepository;
    private final ChatClient chatClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public WorkbookResponse create(CreateWorkbookRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth != null ? auth.getName() : null);
        if (email == null) throw new IllegalStateException("로그인 정보가 없습니다.");

        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다: " + email));

        Workbook wb = Workbook.builder()
                .member(member)
                .language(nullSafe(req.getLanguage()))
                .level(nullSafe(req.getLevel()))
                .style(nullSafe(req.getStyle()))
                .requestDetail(nullSafe(req.getRequestDetail()))
                .topic(nullSafe(req.getTopic()))
                .isUpload(Boolean.FALSE)
                .build();
        workbookRepository.save(wb);

        // ===== 프롬프트 (요청사항 반영: 다양화 + 개념 서술형) =====
        String promptText = buildPrompt(req);

        List<AiQuizDTO> aiQuizzes = callOpenAiAndParse(promptText);
        if (aiQuizzes == null || aiQuizzes.isEmpty()) {
            log.error("AI 퀴즈 생성 실패: 빈 결과");
            throw new IllegalStateException("퀴즈 생성 실패");
        }

        for (AiQuizDTO q : aiQuizzes) {
            String quizBody = """
[문제]
%s

[입력]
%s

[출력]
%s

[예제 입력]
%s
""".formatted(
                    defaultIfBlank(q.statement(), ""),
                    defaultIfBlank(q.inputText(), ""),
                    defaultIfBlank(q.outputText(), ""),
                    defaultIfBlank(q.sampleInput(), "")
            );

            CodingQuiz entity = CodingQuiz.builder()
                    .workbook(wb)
                    .quiz(quizBody)
                    .explanation(defaultIfBlank(q.explanation(), ""))
                    .concept(defaultIfBlank(q.concept(), "")) // ← 서술형 텍스트 수신
                    .isSaved(Boolean.FALSE)
                    .build();

            codingQuizRepository.save(entity);
        }

        return toDto(wb, false, null);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkbookResponse getOneDto(Long id) {
        Workbook wb = workbookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workbook not found: " + id));

        List<CodingQuiz> quizzes = codingQuizRepository.findByWorkbookIdOrderByIdAsc(id);
        List<QuizResponse> quizDtos = quizzes.stream().map(this::toQuizResponse).toList();

        return toDto(wb, true, quizDtos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizView> getQuizzes(Long workbookId) {
        return codingQuizRepository.findByWorkbookIdOrderByIdAsc(workbookId)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkbookResponse> getRecent(int limit) {
        var all = workbookRepository.findAll();
        all.sort((a, b) -> Long.compare(b.getId(), a.getId()));
        return all.stream()
                .limit(limit)
                .map(wb -> toDto(wb, false, null))
                .toList();
    }

    private WorkbookResponse toDto(Workbook wb, boolean includeQuizzes, List<QuizResponse> quizzes) {
        Long memberId = null;
        if (wb.getMember() != null) {
            memberId = Long.valueOf(wb.getMember().getMember_id());
        }

        return WorkbookResponse.builder()
                .id(wb.getId())
                .memberId(memberId)
                .language(wb.getLanguage())
                .level(wb.getLevel())
                .style(wb.getStyle())
                .requestDetail(wb.getRequestDetail())
                .isUpload(wb.getIsUpload())
                .topic(wb.getTopic())
                .quizzes(includeQuizzes ? (quizzes != null ? quizzes : List.of()) : null)
                .build();
    }

    private QuizResponse toQuizResponse(CodingQuiz cq) {
        return QuizResponse.builder()
                .id(cq.getId())
                .workbookId(cq.getWorkbook() != null ? cq.getWorkbook().getId() : null)
                .quiz(defaultIfBlank(cq.getQuiz(), ""))
                .explanation(defaultIfBlank(cq.getExplanation(), ""))
                .concept(defaultIfBlank(cq.getConcept(), ""))
                .isSaved(Boolean.TRUE.equals(cq.getIsSaved()))
                .build();
    }

    private QuizView toView(CodingQuiz cq) {
        Sections sec = splitSections(defaultIfBlank(cq.getQuiz(), ""));
        return QuizView.builder()
                .id(cq.getId())
                .statement(sec.statement)
                .input(sec.input)
                .output(sec.output)
                .sampleInput(sec.sample)
                .explanation(defaultIfBlank(cq.getExplanation(), ""))
                .concept(defaultIfBlank(cq.getConcept(), "")) // ← 서술형 그대로 노출
                .spec(new QuizView.Spec(0L, 0L))
                .build();
    }

    /* ============================== 프롬프트 ============================== */

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
                - 개념은 '키워드 나열'이 아닌 '서술형 설명'으로 4~8줄 작성
                """;
            case "깊이설명" -> """
                - 학습 스타일: 깊이설명
                - 해설은 접근 아이디어 → 복잡도 → 코너케이스 순(8~12줄)
                - 개념은 원리/오용주의/실전팁을 포함해 '서술형' 6~12줄
                """;
            case "예시중심" -> """
                - 학습 스타일: 예시중심
                - 해설은 작은 예제 1~2개로 단계별 풀이 설명(6~10줄)
                - 개념은 예시를 곁들여 '서술형' 5~10줄
                """;
            default -> """
                - 학습 스타일: 일반
                """;
        };

        // ★ 추가: 학습 다양화 규칙
        String diversify = """
            [학습 다양화 규칙]
            - 특정 하위 주제(예: 배열)가 언급되더라도 동일 하위 주제만 반복하지 말 것.
            - 총 10문제 중 '사용자가 특히 어렵다고 한 부분'은 최대 3~4문제까지만 포함.
            - 나머지는 주제와 인접한 개념(문자열, 조건/반복, 자료구조 기초, 간단 알고리즘, 예외/엣지케이스, 실무 응용)을 섞어서 구성.
            - 문제 유형도 다양화: 결과예측/오류찾기/입출력 변형/예제추론/미니구현/설명형 등을 섞기.
            - 난이도 완급조절: 쉬움(3)·보통(4)·살짝도전(3) 정도 권장.
            """;

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
                "concept": "관련 개념 '서술형 설명'(키워드 나열 금지)"
              }
            - 문자열 내 따옴표/개행 등은 유효한 JSON으로 이스케이프.
            - orderNo는 1부터 10까지 증가.
            - %s 언어로 풀이 아이디어를 안내하되, 실제 코드 출력은 하지 않음.
            """).formatted(
                language,
                language, level, style, topic, detail,
                levelRubric, styleRubric,
                diversify,
                language
        );
    }

    private List<AiQuizDTO> callOpenAiAndParse(String promptText) {
        try {
            ChatResponse response = chatClient
                    .prompt(new Prompt(new UserMessage(promptText)))
                    .call()
                    .chatResponse();

            String content = response.getResult().getOutput().getContent();
            String json = sanitizeToJsonArray(content);
            return objectMapper.readValue(json, new TypeReference<List<AiQuizDTO>>() {});
        } catch (Exception e) {
            log.error("AI 호출/파싱 실패", e);
            return null;
        }
    }

    private String sanitizeToJsonArray(String raw) {
        if (raw == null) return "[]";
        String s = raw.trim();
        s = s.replace("```json", "").replace("```", "").trim();
        int start = s.indexOf('['), end = s.lastIndexOf(']');
        if (start >= 0 && end > start) s = s.substring(start, end + 1);
        if (!s.startsWith("[")) return "[]";
        return s;
    }

    private record Sections(String statement, String input, String output, String sample) {}

    private Sections splitSections(String raw) {
        String stmt = "", in = "", out = "", sample = "";
        var p = Pattern.compile("\\[(문제|입력|출력|예제 입력)]");
        var m = p.matcher(raw);
        List<Integer> idx = new ArrayList<>();
        List<String> lab = new ArrayList<>();
        while (m.find()) { idx.add(m.start()); lab.add(m.group(1)); }
        idx.add(raw.length());

        for (int i = 0; i < lab.size(); i++) {
            String label = lab.get(i);
            int from = raw.indexOf(']', idx.get(i)) + 1;
            int to = idx.get(i + 1);
            String body = raw.substring(Math.max(from, 0), Math.max(to, from)).trim();
            switch (label) {
                case "문제" -> stmt = body;
                case "입력" -> in = body;
                case "출력" -> out = body;
                case "예제 입력" -> sample = body;
            }
        }
        return new Sections(stmt, in, out, sample);
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
    private static String defaultIfBlank(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

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
