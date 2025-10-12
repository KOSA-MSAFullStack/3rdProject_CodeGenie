<template>
  <div class="submission-container">
    <!-- 언어 선택 -->
    <div class="toolbar">
      <label for="language-select">언어:</label>
      <select id="language-select" v-model="language">
        <option value="Java">Java</option>
        <option value="Python">Python</option>
        <option value="C++">C++</option>
      </select>
    </div>

    <!-- 모나코 에디터 -->
    <div class="editor-container" ref="editorRef"></div>

    <!-- 제출 버튼 -->
    <div class="actions">
      <button @click="handleSubmit" :disabled="isLoading">
        {{ isLoading ? '채점 중...' : '제출하기' }}
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
          <span>{{ result.time != null ? `${result.time} s` : '-' }}</span>
        </div>
        <div class="grid-item">
          <strong>메모리 사용</strong>
          <span>{{ result.memory != null ? `${result.memory} KB` : '-' }}</span>
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
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as monaco from 'monaco-editor'
import api from '../lib/api'

const props = defineProps({
  quizId: {
    type: Number,
    required: true,
  },
})

const editorRef = ref(null)
let editorInstance = null

const language = ref('Java')
const code = ref('')
const isLoading = ref(false)
const result = ref(null)

const statusClass = ref('')

// Monaco Editor 초기화
onMounted(() => {
  if (editorRef.value) {
    editorInstance = monaco.editor.create(editorRef.value, {
      value: `// 여기에 코드를 작성하세요`,
      language: 'java',
      theme: 'vs-light',
      automaticLayout: true,
    })

    // 코드가 변경될 때마다 ref에 반영
    editorInstance.onDidChangeModelContent(() => {
      code.value = editorInstance.getValue()
    })
  }
})

// 컴포넌트 파괴 전 에디터 인스턴스 정리
onBeforeUnmount(() => {
  if (editorInstance) {
    editorInstance.dispose()
  }
})

// 언어 변경 시 에디터 언어 모드 변경
watch(language, (newLang) => {
  if (editorInstance) {
    const model = editorInstance.getModel()
    if (model) {
      const langMap = { 'Java': 'java', 'Python': 'python', 'C++': 'cpp' };
      monaco.editor.setModelLanguage(model, langMap[newLang] || 'plaintext')
    }
  }
})

// 제출 핸들러
async function handleSubmit() {
  if (!code.value.trim()) {
    alert('코드를 입력하세요.')
    return
  }

  isLoading.value = true
  result.value = null
  statusClass.value = ''

  try {
    const response = await api.post('/submission', {
      quizId: props.quizId,
      answer: code.value,
      language: language.value,
    })
    result.value = response.data

    // 상태에 따라 클래스 부여
    if (result.value.status === 'Accepted') {
        statusClass.value = 'status-accepted';
    } else {
        statusClass.value = 'status-error';
    }

  } catch (error) {
    console.error('Submission failed:', error)
    result.value = {
      status: 'Error',
      stderr: error.response?.data?.message || '채점 서버에 연결할 수 없습니다.',
    }
    statusClass.value = 'status-error';
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.submission-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
}
.toolbar select {
  padding: 6px;
  border-radius: 6px;
  border: 1px solid #ddd;
}
.editor-container {
  width: 100%;
  height: 400px;
  border: 1px solid #ddd;
  border-radius: 8px;
}
.actions {
  display: flex;
  justify-content: flex-end;
}
.actions button {
  border: 0;
  background: #1a4dd9;
  color: #fff;
  border-radius: 20px;
  padding: 10px 20px;
  cursor: pointer;
  font-size: 16px;
}
.actions button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.result-container {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 16px;
}
.result-container h4 {
  margin: 0 0 12px 0;
}
.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}
.grid-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  background: #f7f7f9;
  padding: 12px;
  border-radius: 6px;
}
.grid-item strong {
  font-size: 14px;
  color: #555;
}
.grid-item span {
  font-size: 16px;
  font-weight: bold;
}
.output-box {
  margin-top: 12px;
}
.output-box h5 {
  margin: 0 0 8px 0;
}
.output-box pre {
  background: #f7f7f9;
  color: #333;
  padding: 12px;
  border-radius: 6px;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
}
.output-box.error pre {
  background: #fff0f0;
  color: #d92d20;
}
.status-accepted {
    color: #16a34a;
}
.status-error {
    color: #d92d20;
}
</style>