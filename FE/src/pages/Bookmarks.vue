<template>
  <div class="wrap">
    <!-- 좌측: 문제/제출 -->
    <section class="left">
      <h3 class="title">{{ title }}</h3>

      <!-- 로딩 및 에러 처리 -->
      <div v-if="isLoading">Loading...</div>
      <div v-else-if="error">{{ error }}</div>

      <!-- 빈 상태 -->
      <div v-else-if="!hasProblems" class="empty-wrap">
        <div class="empty-title">이 주제로 저장한 문제가 없습니다.</div>
        <p class="empty-desc">다른 문제집에서 문제를 저장해 보세요.</p>
      </div>

      <template v-else>
        <div class="tabs">
          <button :class="{active: tab==='quiz'}" @click="tab='quiz'">문제</button>
          <button :class="{active: tab==='submit'}" @click="tab='submit'">제출 내역</button>
        </div>

        <!-- 문제 탭 -->
        <div v-if="tab === 'quiz' && cur" class="quiz-area">
          <div class="problem-header">
            <span class="name">문제 {{ cur.quizNumber }}</span>
            <span class="badge">문제</span>
          </div>

          <!-- 제출/정답만 -->
          <table class="spec" v-if="cur.spec">
            <tbody>
              <tr>
                <th>제출</th><td>{{ cur.spec.submissions }}</td>
                <th>정답</th><td>{{ cur.spec.accepted }}</td>
              </tr>
            </tbody>
          </table>

          <section class="block"><h4>문제</h4><p class="statement">{{ cur.statement }}</p></section>
          <section class="block"><h4>입력</h4><p class="statement">{{ cur.input }}</p></section>
          <section class="block"><h4>출력</h4><p class="statement">{{ cur.output }}</p></section>

          <section class="block">
            <h4>예제 입력</h4>
            <textarea class="io" readonly>{{ cur.sampleInput }}</textarea>
          </section>

          <!-- 하단 바 -->
          <div class="footer-bar">
            <div class="foot-left">
            </div>
            <div class="foot-right">
              <button class="btn-subtle" :class="{disabled:isFirst}" @click="goPrev">이전</button>
              <div class="page-no">{{ index + 1 }} / {{ problems.length }}</div>
              <button class="btn-dark" :class="{disabled:isLast}" @click="goNext">다음 문제</button>
            </div>
          </div>
        </div>

        <!-- 제출 탭 -->
        <div v-else-if="tab === 'submit'" class="submit-area">
          <div v-if="cur && cur.lastSubmission" class="submission-details">
            <h4>마지막 제출 기록</h4>
            <p><strong>상태:</strong> {{ cur.lastSubmission.status }}</p>
            <p><strong>제출 시간:</strong> {{ new Date(cur.lastSubmission.submittedAt).toLocaleString() }}</p>
            <pre><code>{{ cur.lastSubmission.answer }}</code></pre>
          </div>
          <div v-else>
            <p>아직 제출 기록이 없습니다.</p>
          </div>
        </div>
      </template>
    </section>

    <!-- 우측: 해설/개념 카드 + 즐겨찾기 버튼 -->
    <aside class="right" v-if="hasProblems && cur">
      <button class="bookmark" title="즐겨찾기 해제" @click="toggleBookmark">
        ❤️
      </button>

      <div class="card">
        <div class="card-title">해설</div>
        <button class="btn" @click="showExplain = !showExplain">{{ showExplain ? '접기' : '보기' }}</button>
      </div>
      <transition name="fade">
        <div v-if="showExplain" class="panel">
          <div class="panel-content">{{ cur.explanation }}</div>
        </div>
      </transition>

      <div class="card">
        <div class="card-title">개념</div>
        <button class="btn" @click="showConcept = !showConcept">{{ showConcept ? '접기' : '보기' }}</button>
      </div>
      <transition name="fade">
        <div v-if="showConcept" class="panel">
          <div class="panel-content">{{ cur.concept }}</div>
        </div>
      </transition>
    </aside>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import authApi from '../lib/authApi';
import { bookmarkUpdateEvent, triggerBookmarkUpdate } from '../lib/eventBus';

const route = useRoute();

// --- 상태 변수 ---
const allSavedBookmarks = ref([]); // API로부터 받은 모든 북마크 원본 목록
const problems = ref([]); // 현재 화면에 표시할 필터링된 퀴즈 목록
const index = ref(0);
const tab = ref('quiz');
const showExplain = ref(false);
const showConcept = ref(false);
const isLoading = ref(true);
const error = ref(null);

// --- Computed 속성 ---
const hasProblems = computed(() => Array.isArray(problems.value) && problems.value.length > 0);

const cur = computed(() => {
  if (!hasProblems.value) return null;
  const originalProblem = problems.value[index.value];
  if (!originalProblem || !originalProblem.quiz) return null;

  const sections = splitSections(defaultIfBlank(originalProblem.quiz.quiz, ""));

  return {
    id: originalProblem.quiz.id,
    quizNumber: originalProblem.quiz.quizNumber,
    statement: sections.statement,
    input: sections.input,
    output: sections.output,
    sampleInput: sections.sample,
    explanation: defaultIfBlank(originalProblem.quiz.explanation, ""),
    concept: defaultIfBlank(originalProblem.quiz.concept, ""),
    isSaved: originalProblem.quiz.is_saved,
    spec: originalProblem.quiz.spec,
    lastSubmission: originalProblem.lastSubmission,
    original: originalProblem 
  };
});

const isFirst = computed(() => index.value === 0);
const isLast = computed(() => index.value === problems.value.length - 1);
const title = computed(() => route.query.topic || '저장한 문제');

// --- 기능 함수 ---
function goPrev() { if (isFirst.value) { alert('첫번째 문제입니다.'); return; } index.value--; }
function goNext() { if (isLast.value) { alert('마지막 문제입니다.'); return; } index.value++; }

async function toggleBookmark() {
  if (!cur.value) return;
  try {
    await authApi.post(`/bookmarks/${cur.value.id}`);
    triggerBookmarkUpdate(); // 전체 목록 갱신을 위해 이벤트 발생
  } catch (err) {
    console.error("Bookmark toggle failed:", err);
    alert("북마크 변경에 실패했습니다.");
  }
}

// --- 헬퍼 함수 ---
function splitSections(raw) {
  const sections = {};
  const regex = /\n*\s*\[(문제|입력|출력|예제 입력)\]\s*\n*/g;
  let lastIndex = 0;
  let match = regex.exec(raw);

  if (!match) {
    return { statement: raw.trim(), input: '', output: '', sample: '' };
  }
  
  let currentLabel = match[1];
  lastIndex = match.index + match[0].length;

  while ((match = regex.exec(raw)) !== null) {
    const sectionText = raw.substring(lastIndex, match.index).trim();
    sections[currentLabel] = sectionText;
    currentLabel = match[1];
    lastIndex = match.index + match[0].length;
  }

  sections[currentLabel] = raw.substring(lastIndex).trim();

  return {
    statement: sections['문제'] || '',
    input: sections['입력'] || '',
    output: sections['출력'] || '',
    sample: sections['예제 입력'] || ''
  };
}

function defaultIfBlank(s, def) {
  return (s == null || String(s).trim() === '') ? def : s;
}

// --- 데이터 처리 ---

// 전체 북마크 목록에서 현재 토픽에 맞는 문제들만 필터링하여 화면에 표시
function filterAndDisplayProblems() {
  const filterTopic = route.query.topic;
  if (filterTopic) {
    problems.value = allSavedBookmarks.value.filter(bookmark => bookmark.quiz.workbookTopic === filterTopic);
  } else {
    // 토픽이 없으면, 모든 북마크를 보여주거나 혹은 빈 목록을 보여줄 수 있음
    // 여기서는 모든 북마크를 보여주도록 처리
    problems.value = allSavedBookmarks.value;
  }
  index.value = 0; // 필터링 후 항상 첫 문제부터 시작
}

// 서버로부터 모든 북마크 데이터를 가져와 캐시에 저장
async function loadAllBookmarks() {
  isLoading.value = true;
  error.value = null;
  try {
    const response = await authApi.get('/bookmarks/saved');
    allSavedBookmarks.value = response.data || [];
    filterAndDisplayProblems(); // 데이터를 가져온 후 현재 토픽에 맞게 필터링
  } catch (err) {
    console.error('북마크 가져오기 실패:', err);
    error.value = '북마크를 불러오는데 실패했습니다.';
    allSavedBookmarks.value = [];
    problems.value = [];
  } finally {
    isLoading.value = false;
  }
}

onMounted(loadAllBookmarks);

// 토픽 변경 시에는 서버 요청 없이 필터링만 다시 수행
watch(() => route.query.topic, filterAndDisplayProblems);

// 북마크 상태가 외부에서 변경되었을 때만 전체 목록을 다시 로드
watch(bookmarkUpdateEvent, loadAllBookmarks);

</script>

<style scoped>
/* Workbooks.vue 스타일 복사 */
.wrap{display:grid;grid-template-columns:1fr 360px;gap:24px;padding:20px}
.left{background:#fff;border:1px solid #eee;border-radius:12px;padding:16px}
.title{margin:0 0 8px 0}
.empty-wrap{display:flex;flex-direction:column;gap:10px;align-items:flex-start;padding:24px 8px}
.empty-title{font-size:18px;font-weight:700}
.empty-desc{color:#666;margin-bottom:6px}
.tabs{display:flex;gap:8px;margin-bottom:12px;border-bottom:1px solid #eee}
.tabs button{border:0;background:transparent;padding:8px 12px;border-bottom:2px solid transparent;cursor:pointer}
.tabs button.active{border-color:#7c8cfb;color:#2036ff;font-weight:700}
.problem-header{display:flex;align-items:center;gap:8px;margin:8px 0 12px}
.problem-header .name{font-weight:700}
.badge{background:#eee;border-radius:6px;padding:2px 6px;font-size:12px}
.spec{width:100%;border-collapse:collapse;background:#fafafa;border:1px solid #eee;border-radius:8px;overflow:hidden;margin-bottom:12px}
.spec th,.spec td{padding:8px;border-bottom:1px solid #eee}
.spec th{width:90px;color:#555}
.block{margin:14px 0}
.block h4{margin:0 0 6px 0}
.statement{white-space:pre-wrap}
.io{width:100%;border:1px solid #ddd;border-radius:8px;padding:8px;height:100px;resize:none;overflow:auto}
.footer-bar{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-top:16px;flex-wrap:wrap}
.foot-left{display:flex;align-items:center;gap:8px}
.foot-right{display:flex;align-items:center;gap:12px}
.btn-outline{border:1px solid #1a4dd9;background:#fff;color:#1a4dd9;border-radius:20px;padding:8px 14px;cursor:pointer}
.btn-subtle{border:1px solid #ddd;background:#fff;border-radius:20px;padding:8px 14px;cursor:pointer}
.btn-dark{border:0;background:#222;color:#fff;border-radius:20px;padding:8px 14px;cursor:pointer}
.btn-subtle.disabled,.btn-dark.disabled{opacity:.45;cursor:not-allowed}
.page-no{color:#666}
.right{position:relative;padding-top:24px}
.bookmark{position:absolute;right:-10px;top:-14px;border:0;background:transparent;font-size:22px;cursor:pointer}
.card{background:#ffe36b;padding:16px;border-radius:12px;min-height:60px;display:flex;align-items:center;justify-content:space-between;margin-bottom:10px}
.card-title{font-weight:700}
.btn{border:0;background:#16a34a;color:#fff;border-radius:20px;padding:8px 14px;cursor:pointer}
.panel{background:#fff;border:1px solid #eee;border-radius:12px;max-height:260px;overflow:auto;padding:12px;margin:-4px 0 14px}
.panel-content{white-space:pre-wrap;line-height:1.45;color:#333}
.fade-enter-active,.fade-leave-active{transition:opacity .18s ease}
.fade-enter-from,.fade-leave-to{opacity:0}

/* 제출 내역 스타일 추가 */
.submission-details {margin-top: 16px;border-top: 1px dashed #ccc;padding-top: 16px;}
pre {background-color: #f5f5f5;padding: 10px;border-radius: 4px;white-space: pre-wrap;word-wrap: break-word;margin: 0;}

@media (max-width:1024px){.wrap{grid-template-columns:1fr}.right{order:-1}}
</style>