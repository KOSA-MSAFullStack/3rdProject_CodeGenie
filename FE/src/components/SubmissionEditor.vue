<template>
  <div class="submission-container">
    <!-- 에디터 -->
    <div class="editor-container" ref="editorRef"></div>

    <!-- 제출 버튼 -->
    <div class="actions">
      <button @click="handleSubmit" :disabled="isLoading">
        {{ isLoading ? "채점 중..." : "제출하기" }}
      </button>
    </div>

    <!-- 결과 표시 -->
    <div v-if="result" class="result-container">
      <h4>채점 결과</h4>
      <div class="result-grid">
        <div class="grid-item">
          <strong>상태</strong>
          <span :class="statusClass">{{ result.status }}</span>
        </div>
        <div class="grid-item">
          <strong>실행 시간</strong>
          <span>{{ result.time != null ? `${result.time} s` : "-" }}</span>
        </div>
        <div class="grid-item">
          <strong>메모리 사용</strong>
          <span>{{ result.memory != null ? `${result.memory} KB` : "-" }}</span>
        </div>
      </div>
      <div v-if="result.stdout" class="output-box">
        <h5>표준 출력</h5>
        <pre>{{ result.stdout }}</pre>
      </div>
      <div v-if="result.stderr" class="output-box error">
        <h5>표준 에러</h5>
        <pre>{{ result.stderr }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from "vue";
import * as monaco from "monaco-editor";
import authApi from "../lib/authApi";

const props = defineProps({
  quizId: { type: Number, required: true },
  /** 워크북 생성 시 사용자가 적은 언어 문자열 (자바/java/Java/파이썬/씨플플 등 자유형식) */
  wbLanguage: { type: String, default: "" },
});

const editorRef = ref(null);
let editorInstance = null;

const code = ref("");
const isLoading = ref(false);
const result = ref(null);
const statusClass = ref("");

// ---- 언어 정규화 & 템플릿 -------------------------------------------------
function normalizeLanguage(raw) {
  if (!raw) return "Java";
  let s = String(raw).trim().toLowerCase();
  if (s.includes("자바")) return "Java";
  if (s.includes("파이썬")) return "Python";
  if ((s.includes("씨") && s.includes("플")) || s.includes("c++")) return "C++";
  s = s.replace(/\s+/g, "");
  if (s.startsWith("java") || s === "jav" || s === "jvaa") return "Java";
  if (s.startsWith("py") || s.startsWith("python")) return "Python";
  if (s.includes("cpp") || s.includes("cxx") || s.includes("c++")) return "C++";
  if (s === "c") return "C++";
  return "Java";
}

const JAVA_TEMPLATE = `import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // 여기에 코드를 입력하세요.

        sc.close();
    }
}
`;

const CPP_TEMPLATE = `#include <bits/stdc++.h>
using namespace std;

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    // 여기에 코드를 입력하세요.

    return 0;
}
`;

const PY_TEMPLATE = `// 여기에 코드를 입력하세요.`;

// Monaco language id 매핑
function monacoLangId(canonical) {
  if (canonical === "Java") return "java";
  if (canonical === "Python") return "python";
  if (canonical === "C++") return "cpp";
  return "plaintext";
}

function starterTemplate(canonical) {
  if (canonical === "Java") return JAVA_TEMPLATE;
  if (canonical === "C++") return CPP_TEMPLATE;
  if (canonical === "Python") return PY_TEMPLATE;
  return "// 여기에 코드를 작성하세요";
}

// ---- Monaco Editor 초기화 -------------------------------------------------
function bootEditor() {
  if (!editorRef.value) return;
  const canonical = normalizeLanguage(props.wbLanguage);
  editorInstance = monaco.editor.create(editorRef.value, {
    value: starterTemplate(canonical),
    language: monacoLangId(canonical),
    theme: "vs-light",
    automaticLayout: true,
  });
  code.value = editorInstance.getValue();

  editorInstance.onDidChangeModelContent(() => {
    code.value = editorInstance.getValue();
  });
}

onMounted(() => {
  bootEditor();
});

// wbLanguage가 바뀌었을 때(일반적이진 않지만) 아직 사용자가 수정 전이면 템플릿/언어 갱신
watch(
  () => props.wbLanguage,
  (nv, ov) => {
    if (!editorInstance) return;
    if (code.value.trim().length > 0 && code.value !== starterTemplate(normalizeLanguage(ov))) {
      // 이미 사용자가 수정했으면 건들지 않음
      return;
    }
    const canonical = normalizeLanguage(nv);
    const model = editorInstance.getModel();
    if (model) {
      monaco.editor.setModelLanguage(model, monacoLangId(canonical));
      editorInstance.setValue(starterTemplate(canonical));
      code.value = editorInstance.getValue();
    }
  }
);

onBeforeUnmount(() => {
  if (editorInstance) editorInstance.dispose();
});

// ---- 제출 -----------------------------------------------------------------
async function handleSubmit() {
  if (!code.value.trim()) {
    alert("코드를 입력하세요.");
    return;
  }

  isLoading.value = true;
  result.value = null;
  statusClass.value = "";

  try {
    // language 필드는 서버에서 무시하므로 빈 문자열 유지 (DTO 호환용)
    const { data } = await authApi.post("/submissions/judge", {
      quizId: props.quizId,
      answer: code.value,
      language: "",
    });

    result.value = data;
    statusClass.value =
      result.value.status === "Accepted" ? "status-accepted" : "status-error";

    // 문제 탭 카운트 실시간 갱신
    const absoluteCountsAvailable =
      typeof result.value.submissions === "number" &&
      typeof result.value.accepted === "number";

    window.dispatchEvent(
      new CustomEvent("quiz:counts", {
        detail: {
          quizId: props.quizId,
          submissions: absoluteCountsAvailable ? result.value.submissions : undefined,
          accepted: absoluteCountsAvailable ? result.value.accepted : undefined,
          delta: 1,
          acceptedDelta: result.value.status === "Accepted" ? 1 : 0,
        },
      })
    );
  } catch (error) {
    console.error("Submission failed:", error);
    result.value = {
      status: "Error",
      stderr: error.response?.data?.message || "채점 서버에 연결할 수 없습니다.",
    };
    statusClass.value = "status-error";
  } finally {
    isLoading.value = false;
  }
}
</script>

<style scoped>
.submission-container { display: flex; flex-direction: column; gap: 16px; }
.editor-container { width: 100%; height: 400px; border: 1px solid #ddd; border-radius: 8px; }
.actions { display: flex; justify-content: flex-end; }
.actions button { border: 0; background: #1a4dd9; color: #fff; border-radius: 20px; padding: 10px 20px; cursor: pointer; font-size: 16px; }
.actions button:disabled { opacity: 0.5; cursor: not-allowed; }
.result-container { border: 1px solid #eee; border-radius: 8px; padding: 16px; }
.result-container h4 { margin: 0 0 12px 0; }
.result-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 12px; margin-bottom: 16px; }
.grid-item { display: flex; flex-direction: column; gap: 4px; background: #f7f7f9; padding: 12px; border-radius: 6px; }
.grid-item strong { font-size: 14px; color: #555; }
.grid-item span { font-size: 16px; font-weight: bold; }
.output-box { margin-top: 12px; }
.output-box h5 { margin: 0 0 8px 0; }
.output-box pre { background: #f7f7f9; color: #333; padding: 12px; border-radius: 6px; white-space: pre-wrap; word-wrap: break-word; margin: 0; }
.output-box.error pre { background: #fff0f0; color: #d92d20; }
.status-accepted { color: #16a34a; }
.status-error { color: #d92d20; }
</style>
