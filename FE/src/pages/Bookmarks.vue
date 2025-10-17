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
          <button :class="{active: tab==='submit'}" @click="tab='submit'">제출</button>
          <button :class="{active: tab==='history'}" @click="tab='history'">제출 내역</button>
        </div>

        <!-- 문제 탭 -->
        <div v-if="tab === 'quiz' && cur" class="quiz-area">
          <div class="problem-header">
            <span class="name">문제 {{ cur.quizNumber }}</span>
            <span class="badge">문제</span>
          </div>

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

          <div class="footer-bar">
            <div class="foot-left"></div>
            <div class="foot-right">
              <button class="btn-subtle" :class="{disabled:isFirst}" @click="goPrev">이전</button>
              <div class="page-no">{{ index + 1 }} / {{ problems.length }}</div>
              <button class="btn-dark" :class="{disabled:isLast}" @click="goNext">다음 문제</button>
            </div>
          </div>
        </div>

        <!-- 제출 탭: 북마크 화면에서도 직접 풀이/채점 -->
        <div v-else-if="tab === 'submit' && cur" class="submit-area">
          <SubmissionEditor :quiz-id="cur.id" />
        </div>

        <!-- 제출 내역 탭: 전체 제출 기록 (최신순) -->
        <div v-else-if="tab === 'history'" class="submit-area">
          <div v-if="isLoadingHistory">Loading...</div>
          <div v-else-if="historyError">{{ historyError }}</div>
          <div v-else-if="submissions.length === 0">
            <p>아직 제출 기록이 없습니다.</p>
          </div>
          <div v-else class="submission-details">
            <h4>제출 내역 (최신순)</h4>
            <div v-for="(s, i) in submissions" :key="s.id || i" style="margin:12px 0;padding:12px;border:1px solid #eee;border-radius:8px;">
              <p><strong>상태:</strong> {{ s.status }}</p>
              <p><strong>제출 시간:</strong> {{ formatDate(s.submittedAt) }}</p>
              <p>
                <strong>실행 시간:</strong> {{ s.time != null ? `${s.time} s` : '-' }} /
                <strong>메모리:</strong> {{ s.memory != null ? `${s.memory} KB` : '-' }}
              </p>
              <div v-if="s.stdout">
                <p><strong>표준 출력</strong></p>
                <pre>{{ s.stdout }}</pre>
              </div>
              <div v-if="s.stderr">
                <p><strong>표준 에러</strong></p>
                <pre>{{ s.stderr }}</pre>
              </div>
              <details>
                <summary>제출 코드 보기</summary>
                <pre style="margin-top:8px"><code>{{ s.answer }}</code></pre>
              </details>
            </div>
          </div>
        </div>
      </template>
    </section>

    <!-- 우측: 해설/개념 카드 (⭐ 저장한 문제 페이지에서는 북마크 버튼 제거) -->
    <aside class="right" v-if="hasProblems && cur">
      <!-- (삭제됨) 북마크 버튼 -->

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
import { bookmarkUpdateEvent } from '../lib/eventBus';
import SubmissionEditor from '../components/SubmissionEditor.vue';

const route = useRoute();

const allSavedBookmarks = ref([]);
const problems = ref([]);
const index = ref(0);
const tab = ref('quiz');
const showExplain = ref(false);
const showConcept = ref(false);
const isLoading = ref(true);
const error = ref(null);

// 제출 내역 상태
const submissions = ref([]);
const isLoadingHistory = ref(false);
const historyError = ref(null);

const isBookmarkPage = computed(() => route.path.includes('/bookmarks'));
const hasProblems = computed(() => Array.isArray(problems.value) && problems.value.length > 0);

const cur = computed(() => {
  if (!hasProblems.value) return null;
  const original = problems.value[index.value];
  if (!original || !original.quiz) return null;

  const sec = splitSections(defaultIfBlank(original.quiz.quiz, ""));
  return {
    id: original.quiz.id,
    quizNumber: original.quiz.quizNumber,
    statement: sec.statement,
    input: sec.input,
    output: sec.output,
    sampleInput: sec.sample,
    explanation: defaultIfBlank(original.quiz.explanation, ""),
    concept: defaultIfBlank(original.quiz.concept, ""),
    isSaved: !!original.quiz.isSaved,
    spec: original.quiz.spec,
    lastSubmission: original.lastSubmission,
    original
  };
});

const isFirst = computed(() => index.value === 0);
const isLast  = computed(() => index.value === problems.value.length - 1);
const title = computed(() => route.query.topic || '저장한 문제');

function goPrev(){ if(isFirst.value){ alert('첫번째 문제입니다.'); return } index.value-- }
function goNext(){ if(isLast.value){ alert('마지막 문제입니다.'); return } index.value++ }

// 제출 내역 불러오기 (최신순 정렬)
async function loadSubmissions() {
  if (!cur.value) return;
  isLoadingHistory.value = true;
  historyError.value = null;
  try {
    // 백엔드 목록 API 경로는 서비스에 맞춰주세요.
    // 예시1) /api/submissions?quizId=123
    // 예시2) /api/submissions/mine?quizId=123
    const { data } = await authApi.get(`/submissions?quizId=${cur.value.id}`);
    const list = Array.isArray(data) ? data : [];
    // 최신순 정렬
    submissions.value = list.sort((a, b) => new Date(b.submittedAt) - new Date(a.submittedAt));
  } catch (e) {
    console.error('제출 내역 로드 실패:', e);
    historyError.value = '제출 내역을 불러오지 못했습니다.';
    submissions.value = [];
  } finally {
    isLoadingHistory.value = false;
  }
}

function formatDate(d) {
  try {
    return new Date(d).toLocaleString();
  } catch {
    return d;
  }
}

function splitSections(raw) {
  const sections = {};
  const regex = /\n*\s*\[(문제|입력|출력|예제 입력)\]\s*\n*/g;
  let lastIndex = 0;
  let match = regex.exec(raw);
  if (!match) return { statement: raw.trim(), input: '', output: '', sample: '' };

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

function filterAndDisplayProblems() {
  const topic = route.query.topic;
  if (topic) {
    problems.value = allSavedBookmarks.value.filter(b => b.quiz.workbookTopic === topic);
  } else {
    problems.value = allSavedBookmarks.value;
  }
  index.value = 0;
  // 현재가 제출 내역 탭이면 새 문제 기준으로 내역 갱신
  if (tab.value === 'history') loadSubmissions();
}

async function loadAllBookmarks() {
  isLoading.value = true;
  error.value = null;
  try {
    const { data } = await authApi.get('/bookmarks/saved');
    allSavedBookmarks.value = Array.isArray(data) ? data : [];
    filterAndDisplayProblems();
  } catch (e) {
    console.error('북마크 가져오기 실패:', e);
    error.value = '북마크를 불러오는데 실패했습니다.';
    allSavedBookmarks.value = [];
    problems.value = [];
  } finally {
    isLoading.value = false;
  }
}

onMounted(loadAllBookmarks);
watch(() => route.query.topic, filterAndDisplayProblems);
watch(bookmarkUpdateEvent, loadAllBookmarks);

// 탭이 '제출 내역'일 때 또는 현재 문제가 바뀔 때 내역 로드
watch([tab, () => cur.value?.id], ([t]) => {
  if (t === 'history') loadSubmissions();
});
</script>

<style scoped>
/* 스타일은 그대로 유지 */
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
.btn-subtle{border:1px solid #ddd;background:#fff;border-radius:20px;cursor:pointer;padding:8px 14px}
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
.submission-details {margin-top: 16px;border-top: 1px dashed #ccc;padding-top: 16px;}
pre {background-color: #f5f5f5;padding: 10px;border-radius: 4px;white-space: pre-wrap;word-wrap: break-word;margin: 0;}
@media (max-width:1024px){.wrap{grid-template-columns:1fr}.right{order:-1}}
</style>
